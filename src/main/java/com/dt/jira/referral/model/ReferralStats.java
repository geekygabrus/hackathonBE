/**
 * 
 */
package com.dt.jira.referral.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author riturajmishra
 */
@Data
@Builder
public class ReferralStats {
    int totalNoOfOpenings;
    int totalNoOfReferrals;
    int totalNoOfHiredCandidates;
    int totalNoOfRejectedCandidates;

}
