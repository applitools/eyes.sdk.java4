package com.applitools.eyes.appium.fluent;

import com.applitools.eyes.*;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.EyesAppiumElement;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

public class AccessibilityRegionByElement extends com.applitools.eyes.selenium.fluent.AccessibilityRegionByElement {

    public AccessibilityRegionByElement(WebElement element, AccessibilityRegionType regionType) {
        super(element, regionType);
    }

    @Override
    public List<AccessibilityRegionByRectangle> getRegions(IEyesBase eyesBase, EyesScreenshot screenshot) {
        EyesAppiumElement eyesAppiumElement = new EyesAppiumElement(((EyesBase)eyesBase).getLogger(),
                ((Eyes) eyesBase).getEyesDriver(), element, 1/((Eyes) eyesBase).getDevicePixelRatio());

        Point p = eyesAppiumElement.getLocation();
        Dimension size = eyesAppiumElement.getSize();

        Location pTag = screenshot.convertLocation(new Location(p.x, p.y), CoordinatesType.CONTEXT_RELATIVE, CoordinatesType.SCREENSHOT_AS_IS);

        return Arrays.asList(new AccessibilityRegionByRectangle(new Region(pTag,
                new RectangleSize(size.width, size.height)), regionType));
    }
}
