/**
 * 
 */
package com.dt.jira.referral.model;

import lombok.Builder;
import lombok.Data;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * @author riturajmishra
 */
@Data
@Builder
public class Notification {
    @JsonProperty("@type")
    private String type;
    @JsonProperty("@context")
    private String context;
    private String themeColor;
    private String summary;
    private List<Section> sections;
}
