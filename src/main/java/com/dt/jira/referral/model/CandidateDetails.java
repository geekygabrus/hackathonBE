/**
 * 
 */
package com.dt.jira.referral.model;

import lombok.Builder;
import lombok.Data;

import org.joda.time.DateTime;

/**
 * @author riturajmishra
 */
@Data
@Builder
public class CandidateDetails {
    private String jiraId;
    private String name;
    private String status;
    private String description;
    private String referredBy;
    private DateTime referredOn;
    private String referralId;
    private String experience;
    private String resumeURL;
    private String email;
}
