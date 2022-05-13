/**
 * 
 */
package com.dt.jira.referral.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author riturajmishra
 */
@Data
@Builder
public class Section {
    private String activityTitle;
    private String activitySubtitle;
    private String activityImage;
    private List<Fact> facts;
    private Boolean markdown;
}
