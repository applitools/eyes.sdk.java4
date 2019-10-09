package com.applitools.eyes.appium.locators;

import com.applitools.eyes.IServerConnector;
import com.applitools.eyes.Logger;
import com.applitools.eyes.Region;
import com.applitools.eyes.VisualLocatorsData;
import com.applitools.eyes.appium.EyesAppiumDriver;
import com.applitools.eyes.locators.IVisualLocatorProvider;
import com.applitools.eyes.locators.IVisualLocatorSettings;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.ImageUtils;

import java.util.List;
import java.util.Map;

public abstract class BaseVisualLocatorProvider implements IVisualLocatorProvider {

    private String viewportScreenshotUrl = null;
    protected Logger logger;
    private IServerConnector serverConnector;
    protected EyesAppiumDriver driver;
    private double devicePixelRatio;

    BaseVisualLocatorProvider(EyesAppiumDriver driver, IServerConnector serverConnector, Logger logger) {
        this.driver = driver;
        this.serverConnector = serverConnector;
        this.logger = logger;
        this.devicePixelRatio = driver.getEyes().getDevicePixelRatio();
    }

    @Override
    public void tryPostScreenshotForLocators() {
        String base64Image = ImageUtils.base64FromImage(getViewPortScreenshot());
        if (base64Image != null) {
            viewportScreenshotUrl = serverConnector.postViewportImage(base64Image);
        }
    }

    @Override
    public Map<String, List<Region>> getLocators(IVisualLocatorSettings visualLocatorSettings) {
        ArgumentGuard.notNull(visualLocatorSettings, "visualLocatorSettings");

        logger.verbose("Get locators with given names: " + visualLocatorSettings.getNames());

        if (viewportScreenshotUrl == null) {
            logger.verbose("Viewport screenshot was not uploaded");
            return null;
        }

        VisualLocatorsData data = new VisualLocatorsData(driver.getEyes().getAppName(), viewportScreenshotUrl, visualLocatorSettings.isFirstOnly(), visualLocatorSettings.getNames());

        Map<String, List<Region>> result = serverConnector.postLocators(data);

        logger.verbose("Done get locators");
        logger.verbose(result.toString());

        return adjustVisualLocators(result);
    }

    double getDevicePixelRatio() {
        return devicePixelRatio;
    }
}
