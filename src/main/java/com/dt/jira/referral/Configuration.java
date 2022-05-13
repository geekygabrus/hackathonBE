/**
 * 
 */
package com.dt.jira.referral;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author riturajmishra
 */
@Component
public class Configuration {

    @Value(value = "${jira.board.url}")
    private String jiraUrl;
    @Value(value = "${jira.board.username}")
    private String jiraUserName;
    @Value(value = "${jira.board.password}")
    private String jiraPassword;

    @Bean
    public JiraRestClient restClient() throws URISyntaxException {
        final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI jiraServerUri = new URI(jiraUrl);
        return factory.createWithBasicHttpAuthentication(jiraServerUri, jiraUserName, jiraPassword);
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper myObjectMapper = new ObjectMapper();
        myObjectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        return myObjectMapper.registerModule(new JodaModule());
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {

            @Override

            public void addCorsMappings(CorsRegistry registry) {

                registry.addMapping("/**").allowedMethods("*");

            }

        };

    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
