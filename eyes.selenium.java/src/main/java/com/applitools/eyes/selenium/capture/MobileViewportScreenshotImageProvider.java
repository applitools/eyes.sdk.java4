package com.applitools.eyes.selenium.capture;

import com.applitools.eyes.Logger;
import com.applitools.eyes.capture.ImageProvider;
import com.applitools.utils.ImageUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

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
        String screenshot64 = (String) ((JavascriptExecutor)driver).executeScript("mobile: viewportScreenshot");
        logger.verbose("Done getting base64! Creating BufferedImage...");
        return ImageUtils.imageFromBase64(screenshot64);
    }
}
