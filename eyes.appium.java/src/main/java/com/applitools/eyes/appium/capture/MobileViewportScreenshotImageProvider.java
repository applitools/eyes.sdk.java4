package com.applitools.eyes.appium.capture;

import com.applitools.eyes.Logger;
import com.applitools.eyes.appium.EyesAppiumDriver;
import com.applitools.eyes.appium.EyesAppiumUtils;
import com.applitools.eyes.capture.ImageProvider;
import com.applitools.utils.ImageUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.awt.image.BufferedImage;

/**
 * An image provider returning viewport screenshots for {@link io.appium.java_client.AppiumDriver}
 */
public class MobileViewportScreenshotImageProvider implements ImageProvider {

    private final Logger logger;
    private final JavascriptExecutor jsExecutor;
    private final TakesScreenshot tsInstance;
    private final Boolean isRunOnTestCloud;

    public MobileViewportScreenshotImageProvider(Logger logger, WebDriver driver) {
        this.logger = logger;
        this.jsExecutor = (JavascriptExecutor) driver;
        this.tsInstance = (TakesScreenshot) driver;
        this.isRunOnTestCloud = (EyesAppiumUtils.isRunOnTestCloud(((EyesAppiumDriver)driver).getRemoteWebDriver()));
    }

    @Override
    public BufferedImage getImage() {
        String screenshot64;
        logger.verbose("Getting screenshot as base64...");
        if (isRunOnTestCloud) {
            screenshot64 = tsInstance.getScreenshotAs(OutputType.BASE64);
        }
        else{
            screenshot64 = (String) jsExecutor.executeScript("mobile: viewportScreenshot");
        }
        logger.verbose("Done getting base64! Creating BufferedImage...");
        return ImageUtils.imageFromBase64(screenshot64);
    }
}