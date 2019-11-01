package com.applitools.eyes.locators;

import com.applitools.eyes.Region;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public interface IVisualLocatorProvider {

    BufferedImage getViewPortScreenshot();

    Map<String, List<Region>> adjustVisualLocators(Map<String, List<Region>> map);

    Map<String, List<Region>> getLocators(IVisualLocatorSettings visualLocatorSettings);
}
