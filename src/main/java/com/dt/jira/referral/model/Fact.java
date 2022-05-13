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
public class Fact {
    private String name;
    private String value;
}
