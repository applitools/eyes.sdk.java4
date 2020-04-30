package com.applitools.eyes.appium.locators;

import com.applitools.eyes.*;
import com.applitools.eyes.appium.EyesAppiumDriver;
import com.applitools.eyes.debug.DebugScreenshotsProvider;
import com.applitools.eyes.locators.IVisualLocatorProvider;
import com.applitools.eyes.locators.IVisualLocatorSettings;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import com.applitools.utils.ImageUtils;
import org.openqa.selenium.OutputType;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        byte[] image = ImageUtils.encodeAsPng(viewPortScreenshot);

        logger.verbose("Post visual locators screenshot...");
        String viewportScreenshotUrl = postViewportImage(image);

        logger.verbose("Screenshot URL: " + viewportScreenshotUrl);

        VisualLocatorsData data = new VisualLocatorsData(driver.getEyes().getAppName(), viewportScreenshotUrl, visualLocatorSettings.isFirstOnly(), visualLocatorSettings.getNames());

        logger.verbose("Post visual locators: " + data.toString());
        Map<String, List<Region>> result = serverConnector.postLocators(data);

        logger.verbose("Done! Response: " + result.toString());
        return adjustVisualLocators(result);
    }

    double getDevicePixelRatio() {
        return devicePixelRatio;
    }

    @SuppressWarnings("Duplicates")
    private String postViewportImage(byte[] bytes) {
        String targetUrl;
        RenderingInfo renderingInfo = serverConnector.getRenderInfo();
        if (renderingInfo != null && (targetUrl = renderingInfo.getResultsUrl()) != null) {
            try {
                UUID uuid = UUID.randomUUID();
                targetUrl = targetUrl.replace("__random__", uuid.toString());
                logger.verbose("uploading viewport image to " + targetUrl);

                int retriesLeft = 3;
                int wait = 500;
                while (retriesLeft-- > 0) {
                    try {
                        int statusCode = serverConnector.uploadData(bytes, renderingInfo, targetUrl, "image/png", "image/png");
                        if (statusCode == 200 || statusCode == 201) {
                            logger.verbose("upload viewport image guid " + uuid + "complete.");
                            return targetUrl;
                        }
                        if (statusCode < 500) {
                            break;
                        }
                    } catch (Exception e) {
                        if (retriesLeft == 0) throw e;
                    }
                    Thread.sleep(wait);
                    wait *= 2;
                    wait = Math.min(10000, wait);
                }
            } catch (Exception e) {
                logger.log("Error uploading viewport image");
                GeneralUtils.logExceptionStackTrace(logger, e);
            }
        }
        return null;
    }
}
