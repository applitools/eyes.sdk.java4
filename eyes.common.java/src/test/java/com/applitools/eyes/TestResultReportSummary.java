package com.applitools.eyes;

import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class TestResultReportSummary {

    private static final String APPLITOOLS_REPORT_ID_KEY = "APPLITOOLS_REPORT_ID";
    private static final String APPLITOOLS_REPORT_TO_SANDBOX_KEY = "TEST_REPORT_SANDBOX";

    private static final String SDK_NAME = "java4";
    private static final String DEFAULT_APPLITOOLS_REPORT_ID = "0000-0000";
    private static final String DEFAULT_GROUP_NAME = "appium";

    @JsonProperty("group")
    private String group = null;

    @JsonProperty("id")
    private String id = null;


    @JsonProperty("sdk")
    public String getSdkName() {
        return SDK_NAME;
    }

    @JsonProperty("id")
    public String getId() {
        if (this.id == null) {
            this.id = GeneralUtils.getEnvString(APPLITOOLS_REPORT_ID_KEY);
        }
        if (this.id == null) {
            return DEFAULT_APPLITOOLS_REPORT_ID;
        }
        return this.id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("sandbox")
    public boolean getSandbox() {
        String isSandbox = GeneralUtils.getEnvString(APPLITOOLS_REPORT_TO_SANDBOX_KEY);
        return !"false".equalsIgnoreCase(isSandbox);
    }

    @JsonProperty("group")
    public String getGroup() {
        if (this.group == null) {
            return DEFAULT_GROUP_NAME;
        }
        return this.group;
    }

    @JsonProperty("group")
    public void setGroup(String group) {
        this.group = group;
    }

    @JsonProperty("results")
    private List<TestResult> testResults = new ArrayList<>();

    @JsonProperty("results")
    public List<TestResult> getTestResults() {
        return testResults;
    }

    public boolean addResult(TestResult result) {
        boolean newResult = !testResults.contains(result);
        testResults.add(result);
        return newResult;
    }

    @Override
    public String toString() {
        return "Group: " + group + " ; Result count: " + testResults.size();
    }
}