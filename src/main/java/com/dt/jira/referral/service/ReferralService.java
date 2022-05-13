package com.dt.jira.referral.service;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.dt.jira.referral.model.CandidateDetails;
import com.dt.jira.referral.model.Fact;
import com.dt.jira.referral.model.JobOpening;
import com.dt.jira.referral.model.JobOpeningsResponse;
import com.dt.jira.referral.model.Notification;
import com.dt.jira.referral.model.ReferralStats;
import com.dt.jira.referral.model.Section;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.atlassian.util.concurrent.Promise;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class ReferralService {

    @Value(value = "${jira.board.url}")
    private String jiraUrl;
    @Value(value = "${jira.board.username}")
    private String jiraUserName;
    @Value(value = "${jira.board.password}")
    private String jiraPassword;
    @Value(value = "${jira.board.project}")
    private String jiraProject;

    @Autowired
    JiraRestClient restClient;

    @Autowired
    RestTemplate restTemplate;

    public BasicIssue createJiraStory(JobOpening jobOpening) throws IOException, URISyntaxException {
        try {

            IssueInput epicInput = new IssueInputBuilder().setProjectKey(jiraProject).setIssueTypeId(10001L)
                    .setDescription(jobOpening.getJobDesc()).setSummary(jobOpening.getJobProfile())
                    .setFieldValue("customfield_10033", jobOpening.getExperienceRange())
                    .setFieldValue("customfield_10034", jobOpening.getDepartment())
                    .setFieldValue("customfield_10035", jobOpening.getNoOfOpenings())
                    .setFieldValue("customfield_10037", jobOpening.getJobDesc()).build();
            BasicIssue basicIssue = restClient.getIssueClient().createIssue(epicInput).claim();
            Notification body = Notification.builder().context("http://schema.org/extensions").type("MessageCard")
                    .themeColor("#FF00FF").summary("New Requirement has been added")
                    .sections(Arrays.asList(Section.builder().activityImage(
                            "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/T-Mobile_logo_2022.svg/1920px-T-Mobile_logo_2022.svg.png")
                            .activitySubtitle(jobOpening.getJobProfile())
                            .activityTitle("Hackfest | New Requirement has been added").markdown(true)
                            .facts(Arrays.asList(
                                    Fact.builder().name("Requirement ID").value(basicIssue.getKey()).build(),
                                    Fact.builder().name("Created on").value(LocalDate.now().toString()).build(),
                                    Fact.builder().name("Experience Range").value(jobOpening.getExperienceRange())
                                            .build(),
                                    Fact.builder().name("Number of Vacancies")
                                            .value(String.valueOf(jobOpening.getNoOfOpenings())).build()))
                            .build()))
                    .build();

            restTemplate.exchange(
                    "https://admintelekomdigital.webhook.office.com/webhookb2/d60ba506-4107-40f1-b533-933b80e4c826@3505a808-a5a7-47c8-ad95-7804005d2734/IncomingWebhook/e66f87f7ae764fb7b054c658d365da4c/963ca4a3-f675-4396-8711-1afd3a08701a",
                    HttpMethod.POST, new HttpEntity<>(body, null), String.class);
            return basicIssue;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            restClient.close();
        }
        return null;
    }

    /**
     * @param opening
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Set<CandidateDetails> getAllSubtasksForStory(String opening)
            throws InterruptedException, ExecutionException {
        SearchResult issue = getLinkedIssues(opening);
        Set<CandidateDetails> children = new HashSet<>();
        issue.getIssues().forEach(i -> {
            if (i.getIssueType().isSubtask()) {
                children.add(getCandidateDetails(i));
            }
        });

        return children;
    }

    private CandidateDetails getCandidateDetails(Issue i) {
        return CandidateDetails.builder().name(String.valueOf(i.getField("customfield_10031").getValue()))
                .jiraId(i.getKey()).status(i.getStatus().getName()).description(i.getDescription())
                .experience(String.valueOf(i.getField("customfield_10029").getValue())).referredOn(i.getCreationDate())
                .referredBy(String.valueOf(i.getField("customfield_10030").getValue())).referralId(i.getKey())
                .resumeURL(String.valueOf(i.getField("customfield_10032").getValue()))
                .email(String.valueOf(i.getField("customfield_10038").getValue())).build();

    }

    /**
     * @param opening
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public CandidateDetails getReferralByNameForGivenOpening(String opening, String referralId)
            throws InterruptedException, ExecutionException {
        SearchResult issue = getLinkedIssues(opening);
        List<CandidateDetails> children = new ArrayList<>();
        issue.getIssues().forEach(i -> {

            if (i.getIssueType().isSubtask() && i.getKey().equalsIgnoreCase(referralId)) {

                children.add(getCandidateDetails(i));
            }
        });
        return children.get(0);
    }

    public SearchResult getLinkedIssues(String opening) {
        Promise<SearchResult> searchResult = restClient.getSearchClient().searchJql("linkedissue =" + opening);
        SearchResult issue = searchResult.claim();
        return issue;
    }

    public ReferralStats getStats() {
        String jql = "project=" + jiraProject;
        Promise<SearchResult> searchResult = restClient.getSearchClient().searchJql(jql);
        SearchResult issue = searchResult.claim();

        List<JobOpeningsResponse> jobOpeningsResponses = new ArrayList<>();
        List<String> children = new ArrayList<>();
        List<String> hired = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        // Gets all subtask
        issue.getIssues().forEach(i -> {

            if (i.getIssueType().isSubtask() == false) {
                JobOpeningsResponse jobOpeningsResponse = new JobOpeningsResponse();
                jobOpeningsResponses.add(jobOpeningsResponse);
                i.getSubtasks().forEach(j -> {
                    if (j.getIssueType().isSubtask()) {
                        children.add(j.getIssueKey());
                        if ("Hired".equalsIgnoreCase(j.getStatus().getName())) {
                            hired.add(j.getIssueKey());
                        }
                        if ("Rejected".equalsIgnoreCase(j.getStatus().getName())) {
                            rejected.add(j.getIssueKey());
                        }
                    }

                });
            }

        });

        return ReferralStats.builder().totalNoOfOpenings(jobOpeningsResponses.size())
                .totalNoOfHiredCandidates(hired.size()).totalNoOfReferrals(children.size())
                .totalNoOfRejectedCandidates(rejected.size()).build();
    }

    public List<JobOpeningsResponse> getAllOpenings(String key) {
        String jql = key == null ? "project=" + jiraProject : "project=" + jiraProject + " and key = " + key;
        Promise<SearchResult> searchResult = restClient.getSearchClient().searchJql(jql);
        SearchResult issue = searchResult.claim();

        List<JobOpeningsResponse> jobOpeningsResponses = new ArrayList<>();

        // Gets all subtask
        issue.getIssues().forEach(i -> {
            if (i.getIssueType().isSubtask() == false) {
                JobOpeningsResponse jobOpeningsResponse = new JobOpeningsResponse();
                Set<String> children = new HashSet<>();
                i.getSubtasks().forEach(j -> {
                    if (j.getIssueType().isSubtask()) {
                        children.add(j.getIssueKey());
                    }

                });
                jobOpeningsResponse.setTitle(i.getSummary());
                jobOpeningsResponse.setJobDesc(i.getDescription());
                jobOpeningsResponse.setPostedOn(i.getCreationDate());
                jobOpeningsResponse.setNoOfApplicants(children.size());
                jobOpeningsResponse.setExperience(String.valueOf(i.getField("customfield_10033").getValue()));
                jobOpeningsResponse.setJiraId(i.getKey());
                jobOpeningsResponse.setDepartment(String.valueOf(i.getField("customfield_10034").getValue()));
                jobOpeningsResponses.add(jobOpeningsResponse);
            }

        });

        return jobOpeningsResponses;
    }

    /**
     * update jira issue status status: like "Done"
     */
    public void updateIssueStatus(String opening, String referralId, String status) {
        IssueRestClient client = restClient.getIssueClient();
        SearchResult issue = getLinkedIssues(opening);
        List<Issue> children = new ArrayList<>();
        issue.getIssues().forEach(i -> {
            if (i.getIssueType().isSubtask() && i.getKey().equalsIgnoreCase(referralId)) {
                children.add(i);
            }
        });

        Iterable<Transition> transitions = client.getTransitions(children.get(0)).claim();

        for (Transition t : transitions) {

            if (t.getName().equalsIgnoreCase(status)) {
                TransitionInput input = new TransitionInput(t.getId());
                client.transition(children.get(0), input).claim();
                return;
            }
        }
    }

}
