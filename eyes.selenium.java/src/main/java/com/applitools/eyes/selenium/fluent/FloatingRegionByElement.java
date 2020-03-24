package com.applitools.eyes.selenium.fluent;

import com.applitools.eyes.*;
import com.applitools.eyes.fluent.GetFloatingRegion;
import com.applitools.eyes.selenium.Eyes;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class FloatingRegionByElement implements GetFloatingRegion {

    protected WebElement element;
    protected int maxUpOffset;
    protected int maxDownOffset;
    protected int maxLeftOffset;
    protected int maxRightOffset;

    public FloatingRegionByElement(WebElement element, int maxUpOffset, int maxDownOffset, int maxLeftOffset, int maxRightOffset) {

        this.element = element;
        this.maxUpOffset = maxUpOffset;
        this.maxDownOffset = maxDownOffset;
        this.maxLeftOffset = maxLeftOffset;
        this.maxRightOffset = maxRightOffset;
    }

    @Override
    public List<FloatingMatchSettings> getRegions(EyesBase eyesBase, EyesScreenshot screenshot) {
        Point locationAsPoint = element.getLocation();
        Dimension size = element.getSize();

        // Element's coordinates are context relative, so we need to convert them first.
        Location adjustedLocation = screenshot.getLocationInScreenshot(new Location(locationAsPoint.getX(), locationAsPoint.getY()),
                CoordinatesType.CONTEXT_RELATIVE);

        List<FloatingMatchSettings> value = new ArrayList<>();

        value.add(new FloatingMatchSettings(adjustedLocation.getX(), adjustedLocation.getY(), size.getWidth(),
                size.getHeight(), maxUpOffset, maxDownOffset, maxLeftOffset, maxRightOffset));

        return value;
    }
}
