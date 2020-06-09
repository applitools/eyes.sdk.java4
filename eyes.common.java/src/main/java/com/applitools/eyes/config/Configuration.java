package com.applitools.eyes.config;

import com.applitools.eyes.*;
import com.applitools.utils.GeneralUtils;

public class Configuration {

    private BatchInfo batch;
    private String branchName = GeneralUtils.getEnvString(GeneralUtils.APPLITOOLS_BRANCH);
    private String parentBranchName = GeneralUtils.getEnvString(GeneralUtils.APPLITOOLS_PARENT_BRANCH);
    private String baselineBranchName = GeneralUtils.getEnvString(GeneralUtils.APPLITOOLS_BASELINE_BRANCH);
    private String agentId;
    private String baselineEnvName;
    private String environmentName;
    private Boolean saveDiffs;
    private String appName;
    private String testName;
    private RectangleSize viewportSize;
    private SessionType sessionType;
    private AccessibilitySettings accessibilityValidation = null;
    private boolean ignoreDisplacements = false;
    private boolean enablePatterns = false;
    private Integer stitchingAdjustment;

    public void setBatch(BatchInfo batch) {
        this.batch = batch;
    }

    public BatchInfo getBatch() {
        return batch;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getParentBranchName() {
        return parentBranchName;
    }

    public void setParentBranchName(String parentBranchName) {
        this.parentBranchName = parentBranchName;
    }

    public String getBaselineBranchName() {
        return baselineBranchName;
    }

    public void setBaselineBranchName(String baselineBranchName) {
        this.baselineBranchName = baselineBranchName;
    }

    public String getBaselineEnvName() {
        return baselineEnvName;
    }

    public void setBaselineEnvName(String baselineEnvName) {
        this.baselineEnvName = baselineEnvName;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public Boolean getSaveDiffs() {
        return saveDiffs;
    }

    public void setSaveDiffs(Boolean saveDiffs) {
        this.saveDiffs = saveDiffs;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public RectangleSize getViewportSize() {
        return viewportSize;
    }

    public void setViewportSize(RectangleSize viewportSize) {
        this.viewportSize = viewportSize;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public AccessibilitySettings getAccessibilityValidation() {
        return accessibilityValidation;
    }

    public void setAccessibilityValidation(AccessibilitySettings accessibilityValidation) {
        if (accessibilityValidation == null) {
            this.accessibilityValidation = null;
            return;
        }

        if (accessibilityValidation.getLevel() == null || accessibilityValidation.getGuidelinesVersion() == null) {
            throw new IllegalArgumentException("AccessibilitySettings should have the following properties: ‘level,version’");
        }

        this.accessibilityValidation = accessibilityValidation;
    }

    public boolean getIgnoreDisplacements() {
        return ignoreDisplacements;
    }

    public void setIgnoreDisplacements(boolean ignoreDisplacements) {
        this.ignoreDisplacements = ignoreDisplacements;
    }

    public boolean getEnablePatterns() {
        return enablePatterns;
    }

    public void setEnablePatterns(boolean enablePatterns) {
        this.enablePatterns = enablePatterns;
    }

    public Integer getStitchingAdjustment() {
        return stitchingAdjustment;
    }

    /**
     * This parameter will help to avoid shadows on stitching parts.
     * Used only for full page screenshots.
     */
    public void setStitchingAdjustment(Integer stitchingAdjustment) {
        this.stitchingAdjustment = stitchingAdjustment;
    }
}
