package com.applitools.eyes.selenium.fluent;

import com.applitools.eyes.*;
import com.applitools.eyes.fluent.GetRegion;
import com.applitools.eyes.selenium.Eyes;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class SimpleRegionBySelector implements GetRegion {
    private By selector;

    public SimpleRegionBySelector(By selector) {
        this.selector = selector;
    }

    @Override
    public List<Region> getRegions(EyesBase eyesBase, EyesScreenshot screenshot, boolean adjustLocation) {
        List<WebElement> elements = ((Eyes)eyesBase).getDriver().findElements(this.selector);
        List<Region> values = new ArrayList<>(elements.size());
        for (WebElement element : elements) {

            Point locationAsPoint = element.getLocation();
            Dimension size = element.getSize();

            // Element's coordinates are context relative, so we need to convert them first.
            Location adjustedLocation = screenshot.getLocationInScreenshot(
                    new Location(locationAsPoint.getX(), locationAsPoint.getY()),
                    CoordinatesType.CONTEXT_RELATIVE);

            values.add(new Region(adjustedLocation,  new RectangleSize(size.getWidth(), size.getHeight()),
                    CoordinatesType.SCREENSHOT_AS_IS));
        }
        return values;
    }
}
