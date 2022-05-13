/**
 * 
 */
package com.dt.jira.referral.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author riturajmishra
 */
public class Test {

    public static void main(String[] args) throws IOException, URISyntaxException {
        final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI jiraServerUri = new URI("https://mishra-rituraj96.atlassian.net");
        final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri,
                "mishrarituraj96@gmail.com", "4Qg9HP7HjV8moEbtN2lx6EC2");
        try {
            IssueInput epicInput = new IssueInputBuilder()//
                    .setProjectKey("REF")//
                    .setIssueTypeId(10001L)//
                    .setSummary("Epic W/Rest API")//
                    .setDescription("my epic description")//
                    .build();
            BasicIssue epicIssue = restClient.getIssueClient().createIssue(epicInput).claim();
            IssueInput subTaskInput = new IssueInputBuilder()//
                    .setProjectKey("Project Key")//
                    .setIssueTypeId(10003L)//
                    .setSummary("Subtask W/Rest API")//
                    .setDescription("my subtask description")//
                    .setFieldValue("parent", ComplexIssueInputFieldValue.with("key", epicIssue.getKey()))//
                    .build();
            restClient.getIssueClient().createIssue(subTaskInput);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            restClient.close();
        }
    }

}
