/**
 * 
 */
package com.dt.jira.referral.controller;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.dt.jira.referral.model.CandidateDetails;
import com.dt.jira.referral.model.JobOpening;
import com.dt.jira.referral.model.JobOpeningsResponse;
import com.dt.jira.referral.model.ReferralStats;
import com.dt.jira.referral.service.ReferralService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author riturajmishra
 */
@RestController
@RequestMapping("/referral/v1")
public class ReferralJiraController {

    @Autowired
    ReferralService referralService;

    @PostMapping("/create-job-opening")
    public ResponseEntity<?> createOpening(@RequestBody JobOpening jobOpening) throws IOException, URISyntaxException {
        BasicIssue basicIssue = referralService.createJiraStory(jobOpening);
        if (null != basicIssue)
            return ResponseEntity.ok("Issue created for :" + basicIssue.getKey());
        else
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to create");

    }

    @GetMapping("/opening/{opening}/referrals")
    public ResponseEntity<?> getAllReferralsForOpening(@PathVariable String opening)
            throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        Set<CandidateDetails> childeIssues = referralService.getAllSubtasksForStory(opening);
        if (!CollectionUtils.isEmpty(childeIssues))
            return ResponseEntity.ok(childeIssues);
        else
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to find any openings");

    }

    @GetMapping("/opening/{opening}/referral/{referralId}")
    public ResponseEntity<?> getReferralByNameForGivenOpening(@PathVariable String opening,
            @PathVariable String referralId)
            throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        CandidateDetails candidate = referralService.getReferralByNameForGivenOpening(opening, referralId);
        if (null != candidate)
            return ResponseEntity.ok(candidate);
        else
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to find any openings");

    }

    @GetMapping("/openings")
    public ResponseEntity<?> getAllOpenings(@RequestParam(required = false) String key)
            throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        List<JobOpeningsResponse> searchResults = referralService.getAllOpenings(key);
        if (!CollectionUtils.isEmpty(searchResults))
            return ResponseEntity.ok(searchResults);
        else
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to find any openings");

    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats()
            throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        ReferralStats referralStats = referralService.getStats();
        if (null != referralStats)
            return ResponseEntity.ok(referralStats);
        else
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed to fetch stats");

    }

    @GetMapping("/opening/{opening}/referral/{referralId}/status/{status}")
    public ResponseEntity<?> updateReferralStatus(@PathVariable String opening, @PathVariable String referralId,
            @PathVariable String status)
            throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        referralService.updateIssueStatus(opening, referralId, status);
        return ResponseEntity.ok("Status updated");
    }

}
