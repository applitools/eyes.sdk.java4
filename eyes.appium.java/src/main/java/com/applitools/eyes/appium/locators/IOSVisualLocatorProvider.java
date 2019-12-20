package com.applitools.eyes.appium.locators;

import com.applitools.eyes.IServerConnector;
import com.applitools.eyes.Logger;
import com.applitools.eyes.Region;
import com.applitools.eyes.appium.EyesAppiumDriver;

import java.util.List;
import java.util.Map;

public class IOSVisualLocatorProvider extends BaseVisualLocatorProvider {

    IOSVisualLocatorProvider(EyesAppiumDriver driver, Logger logger, IServerConnector serverConnector) {
        super(driver, serverConnector, logger);
    }

    @Override
    public Map<String, List<Region>> adjustVisualLocators(Map<String, List<Region>> map) {
        return map;
    }
}
