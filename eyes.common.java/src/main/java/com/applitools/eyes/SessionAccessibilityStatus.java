package com.applitools.eyes;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionAccessibilityStatus {
    public enum AccessibilityStatus {
        Passed,
        Failed
    }

    @JsonProperty("status")
    private AccessibilityStatus status;
    @JsonProperty("level")
    private AccessibilityLevel level;
    @JsonProperty("version")
    private AccessibilityGuidelinesVersion version;

    public SessionAccessibilityStatus() {}

    public SessionAccessibilityStatus(AccessibilityStatus status, AccessibilitySettings settings) {
        this.status = status;
        this.level = settings.getLevel();
        this.version = settings.getGuidelinesVersion();
    }

    public AccessibilityStatus getStatus() {
        return status;
    }

    public AccessibilityLevel getLevel() {
        return level;
    }

    public AccessibilityGuidelinesVersion getVersion() {
        return version;
    }
}
