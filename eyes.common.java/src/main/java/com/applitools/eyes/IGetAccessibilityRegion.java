package com.applitools.eyes;

import java.util.List;

public interface IGetAccessibilityRegion {

    List<AccessibilityRegionByRectangle> getRegions(IEyesBase eyesBase, EyesScreenshot screenshot);
}
