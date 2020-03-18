package com.applitools.eyes.appium.fluent;

import com.applitools.eyes.*;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.EyesAppiumElement;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class FloatingRegionByElement extends com.applitools.eyes.selenium.fluent.FloatingRegionByElement {

    public FloatingRegionByElement(WebElement element, int maxUpOffset, int maxDownOffset, int maxLeftOffset, int maxRightOffset) {
        super(element, maxUpOffset, maxDownOffset, maxLeftOffset, maxRightOffset);
    }

    @Override
    public List<FloatingMatchSettings> getRegions(EyesBase eyesBase, EyesScreenshot screenshot) {
        EyesAppiumElement eyesAppiumElement = new EyesAppiumElement(eyesBase.getLogger(),
                ((Eyes) eyesBase).getEyesDriver(), element, 1/((Eyes) eyesBase).getDevicePixelRatio());

        Point locationAsPoint = eyesAppiumElement.getLocation();
        Dimension size = eyesAppiumElement.getSize();

        List<FloatingMatchSettings> value = new ArrayList<>();

        value.add(new FloatingMatchSettings(locationAsPoint.getX(), locationAsPoint.getY(), size.getWidth(),
                size.getHeight(), maxUpOffset, maxDownOffset, maxLeftOffset, maxRightOffset));

        return value;
    }
}
