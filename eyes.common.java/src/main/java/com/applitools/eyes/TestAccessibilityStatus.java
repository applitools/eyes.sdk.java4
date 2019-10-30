package com.applitools.eyes;

public class TestAccessibilityStatus {
    private  AccessibilityStatus status;

    private AccessibilityLevel level;

    public AccessibilityStatus getStatus() {
        return status;
    }

    public void setStatus(AccessibilityStatus status) {
        this.status = status;
    }

    public AccessibilityLevel getLevel() {
        return level;
    }

    public void setLevel(AccessibilityLevel level) {
        this.level = level;
    }

    public enum AccessibilityStatus {
        Passed,
        Failed
    }
}
