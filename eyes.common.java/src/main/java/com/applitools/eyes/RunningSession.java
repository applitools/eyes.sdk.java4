/*
 * Applitools SDK for Selenium integration.
 */
package com.applitools.eyes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Encapsulates data for the session currently running in the agent.
 */
@JsonIgnoreProperties({"isNewSession", "$id", "steps"})
public class RunningSession {
    private boolean isNewSession;
    private String id;
    private String url;
    private String baselineId;
    private String batchId;
    private String sessionId;
    private Boolean isNew;

    public RunningSession() {
        isNewSession = false;
    }

    public boolean getIsNewSession() {
        return isNewSession;
    }

    public void setIsNewSession(boolean isNewSession) {
        this.isNewSession = isNewSession;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBaselineId() {
        return baselineId;
    }

    public void setBaselineId(String baselineId) {
        this.baselineId = baselineId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean aNew) {
        isNew = aNew;
    }
}
