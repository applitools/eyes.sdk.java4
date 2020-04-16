package com.applitools.eyes.fluent;

import com.applitools.eyes.IGetAccessibilityRegion;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.Region;

public interface ICheckSettingsInternal {
    Region getTargetRegion();
    int getTimeout();
    boolean getStitchContent();
    MatchLevel getMatchLevel();
    GetRegion[] getIgnoreRegions();
    GetRegion[] getStrictRegions();
    GetRegion[] getLayoutRegions();
    GetRegion[] getContentRegions();
    GetFloatingRegion[] getFloatingRegions();
    Boolean getIgnoreCaret();
    String getName();
    IGetAccessibilityRegion[] getAccessibilityRegions();
    Boolean isUseDom();
    Boolean isSendDom();
}
