package com.applitools.eyes.appium.locators;

import com.applitools.eyes.IServerConnector;
import com.applitools.eyes.Logger;
import com.applitools.eyes.Region;
import com.applitools.eyes.VisualLocatorsData;
import com.applitools.eyes.appium.EyesAppiumDriver;
import com.applitools.eyes.debug.DebugScreenshotsProvider;
import com.applitools.eyes.locators.IVisualLocatorProvider;
import com.applitools.eyes.locators.IVisualLocatorSettings;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public abstract class BaseVisualLocatorProvider implements IVisualLocatorProvider {

    protected Logger logger;
    private IServerConnector serverConnector;
    protected EyesAppiumDriver driver;
    private double devicePixelRatio;
    private DebugScreenshotsProvider debugScreenshotsProvider;

    BaseVisualLocatorProvider(EyesAppiumDriver driver, IServerConnector serverConnector, Logger logger, DebugScreenshotsProvider debugScreenshotsProvider) {
        this.driver = driver;
        this.serverConnector = serverConnector;
        this.logger = logger;
        this.devicePixelRatio = driver.getEyes().getDevicePixelRatio();
        this.debugScreenshotsProvider = debugScreenshotsProvider;
    }

    @Override
    public Map<String, List<Region>> getLocators(IVisualLocatorSettings visualLocatorSettings) {
        ArgumentGuard.notNull(visualLocatorSettings, "visualLocatorSettings");

        logger.verbose("Get locators with given names: " + visualLocatorSettings.getNames());

        BufferedImage viewPortScreenshot = getViewPortScreenshot();
        debugScreenshotsProvider.save(viewPortScreenshot, "visual_locators_screenshot");
        String base64Image = ImageUtils.base64FromImage(viewPortScreenshot);
        String viewportScreenshotUrl = serverConnector.postViewportImage(base64Image);

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
