/**
 * 
 */
package com.dt.jira.referral.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author riturajmishra
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JobOpening {
    private String experienceRange;
    private String jobDesc;
    private String department;
    private int noOfOpenings;
    private String jobProfile;

}
