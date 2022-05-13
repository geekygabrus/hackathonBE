/**
 * 
 */
package com.dt.jira.referral.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.joda.time.DateTime;

/**
 * @author riturajmishra
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JobOpeningsResponse {
    private String jiraId;
    private String title;
    private int noOfApplicants;
    private String experience;
    private String jobDesc;
    private DateTime postedOn;
    private String department;

}
