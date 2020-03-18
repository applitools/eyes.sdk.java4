package com.applitools.eyes.selenium.fluent;

import com.applitools.eyes.*;
import com.applitools.eyes.fluent.GetRegion;
import com.applitools.eyes.selenium.Eyes;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class SimpleRegionByElement implements GetRegion {
    protected WebElement element;

    public SimpleRegionByElement(WebElement element) {
        this.element = element;
    }

    @Override
    public List<Region> getRegions(EyesBase eyesBase, EyesScreenshot screenshot, boolean adjustLocation) {
        Point locationAsPoint = element.getLocation();
        Dimension size = element.getSize();

        // Element's coordinates are context relative, so we need to convert them first.
        Location adjustedLocation = screenshot.getLocationInScreenshot(new Location(locationAsPoint.getX(), locationAsPoint.getY()),
                ((Eyes)eyesBase).getElementOriginalLocation(element),
                CoordinatesType.CONTEXT_RELATIVE);

        List<Region> value = new ArrayList<>();
        value.add(new Region(adjustedLocation, new RectangleSize(size.getWidth(), size.getHeight()),
                CoordinatesType.SCREENSHOT_AS_IS));

        return value;
    }
}
