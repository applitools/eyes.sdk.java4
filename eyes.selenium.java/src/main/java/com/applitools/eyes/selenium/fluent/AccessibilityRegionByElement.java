package com.applitools.eyes.selenium.fluent;

import com.applitools.eyes.*;
import com.applitools.eyes.fluent.IGetAccessibilityRegionType;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

public class AccessibilityRegionByElement implements IGetAccessibilityRegion, IGetSeleniumRegion, IGetAccessibilityRegionType {

    private AccessibilityRegionType regionType;
    private WebElement element;

    public AccessibilityRegionByElement(WebElement element, AccessibilityRegionType regionType) {
        this.element = element;
        this.regionType = regionType;
    }

    @Override
    public List<AccessibilityRegionByRectangle> getRegions(IEyesBase eyesBase, EyesScreenshot screenshot) {
        Point p = element.getLocation();
        Location pTag = screenshot.convertLocation(new Location(p.x, p.y), CoordinatesType.CONTEXT_RELATIVE, CoordinatesType.SCREENSHOT_AS_IS);

        return Arrays.asList(new AccessibilityRegionByRectangle(new Region(pTag, new RectangleSize(element.getSize().width, element.getSize().height)), regionType));
    }


    @Override
    public AccessibilityRegionType getAccessibilityRegionType() {
        return regionType;
    }

    @Override
    public List<WebElement> getElements(WebDriver driver) {
        return Arrays.asList(element);
    }

}
