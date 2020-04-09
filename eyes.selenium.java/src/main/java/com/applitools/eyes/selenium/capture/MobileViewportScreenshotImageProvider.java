package com.applitools.eyes.selenium.capture;

import com.applitools.eyes.Logger;
import com.applitools.eyes.capture.ImageProvider;
import com.applitools.utils.ImageUtils;
import org.openqa.selenium.*;

import java.awt.image.BufferedImage;

public class MobileViewportScreenshotImageProvider implements ImageProvider {

    private final Logger logger;
    private final WebDriver driver;

    public MobileViewportScreenshotImageProvider(Logger logger, WebDriver driver) {
        this.logger = logger;
        this.driver = driver;
    }

    @Override
    public BufferedImage getImage() {
        logger.verbose("Getting screenshot as base64...");
        String screenshot64;
        try {
            logger.verbose("Trying viewportScreenshot command..");
            screenshot64 = (String) ((JavascriptExecutor) driver).executeScript("mobile: viewportScreenshot");
            logger.verbose("Done!");
        } catch (WebDriverException e) {
            logger.verbose("Got the following exception for viewportScreenshot command: " + e);
            logger.verbose("Using standard screenshot instead...");
            screenshot64 = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BASE64);
            logger.verbose("Done!");
        }
        logger.verbose("Done getting base64! Creating BufferedImage...");
        return ImageUtils.imageFromBase64(screenshot64);
    }
}
