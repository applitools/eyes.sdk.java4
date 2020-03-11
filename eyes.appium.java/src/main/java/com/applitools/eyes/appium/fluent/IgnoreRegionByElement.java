package com.applitools.eyes.appium.fluent;

import com.applitools.eyes.*;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.EyesAppiumElement;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class IgnoreRegionByElement extends com.applitools.eyes.selenium.fluent.IgnoreRegionByElement {

    public IgnoreRegionByElement(WebElement element) {
        super(element);
    }

    @Override
    public List<Region> getRegions(EyesBase eyesBase, EyesScreenshot screenshot, boolean adjustLocation) {
        EyesAppiumElement eyesAppiumElement = new EyesAppiumElement(eyesBase.getLogger(),
                ((Eyes) eyesBase).getEyesDriver(), element, 1/((Eyes) eyesBase).getDevicePixelRatio());

        Point locationAsPoint = eyesAppiumElement.getLocation();
        Dimension size = eyesAppiumElement.getSize();

        List<Region> value = new ArrayList<>();
        value.add(new Region(new Location(locationAsPoint.getX(), locationAsPoint.getY()), new RectangleSize(size.getWidth(), size.getHeight()),
                CoordinatesType.SCREENSHOT_AS_IS));

        return value;
    }
}
