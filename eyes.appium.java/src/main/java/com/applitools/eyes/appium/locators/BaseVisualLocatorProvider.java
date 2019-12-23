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
import org.openqa.selenium.OutputType;

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

    private BufferedImage getViewPortScreenshot() {
        logger.verbose("Getting screenshot as base64...");
        String base64Image = driver.getScreenshotAs(OutputType.BASE64);

        logger.verbose("Done getting base64! Creating BufferedImage...");
        BufferedImage image = ImageUtils.imageFromBase64(base64Image);

        logger.verbose("Scale image with the scale ratio - " + 1/getDevicePixelRatio());
        return ImageUtils.scaleImage(image, 1/getDevicePixelRatio());
    }

    @Override
    public Map<String, List<Region>> getLocators(IVisualLocatorSettings visualLocatorSettings) {
        ArgumentGuard.notNull(visualLocatorSettings, "visualLocatorSettings");

        logger.verbose("Get locators with given names: " + visualLocatorSettings.getNames());

        logger.verbose("Requested viewport screenshot for visual locators...");
        BufferedImage viewPortScreenshot = getViewPortScreenshot();
        debugScreenshotsProvider.save(viewPortScreenshot, "visual_locators_" + String.join("_", visualLocatorSettings.getNames()));

        logger.verbose("Convert screenshot from BufferedImage to base64...");
        String base64Image = ImageUtils.base64FromImage(viewPortScreenshot);

        logger.verbose("Post visual locators screenshot...");
        String viewportScreenshotUrl = serverConnector.postViewportImage(base64Image);

        VisualLocatorsData data = new VisualLocatorsData(driver.getEyes().getAppName(), viewportScreenshotUrl, visualLocatorSettings.isFirstOnly(), visualLocatorSettings.getNames());

        logger.verbose("Post visual locators: " + data.toString());
        Map<String, List<Region>> result = serverConnector.postLocators(data);

        logger.verbose("Done! Response: " + result.toString());
        return adjustVisualLocators(result);
    }

    double getDevicePixelRatio() {
        return devicePixelRatio;
    }
}
