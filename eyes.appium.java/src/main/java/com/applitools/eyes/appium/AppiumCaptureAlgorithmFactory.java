package com.applitools.eyes.appium;

import com.applitools.eyes.CutProvider;
import com.applitools.eyes.Logger;
import com.applitools.eyes.ScaleProviderFactory;
import com.applitools.eyes.capture.EyesScreenshotFactory;
import com.applitools.eyes.capture.ImageProvider;
import com.applitools.eyes.debug.DebugScreenshotsProvider;
import org.openqa.selenium.WebElement;

public class AppiumCaptureAlgorithmFactory {

    private EyesAppiumDriver driver;
    private Logger logger;
    private AppiumScrollPositionProvider scrollProvider;
    private ImageProvider imageProvider;
    private DebugScreenshotsProvider debugScreenshotsProvider;
    private ScaleProviderFactory scaleProviderFactory;
    private CutProvider cutProvider;
    private EyesScreenshotFactory screenshotFactory;
    private int waitBeforeScreenshot;
    private WebElement cutElement;
    private Integer stitchingAdjustment;

    public AppiumCaptureAlgorithmFactory(EyesAppiumDriver driver, Logger logger,
                                         AppiumScrollPositionProvider scrollProvider,
                                         ImageProvider imageProvider, DebugScreenshotsProvider debugScreenshotsProvider,
                                         ScaleProviderFactory scaleProviderFactory, CutProvider cutProvider,
                                         EyesScreenshotFactory screenshotFactory, int waitBeforeScreenshots, WebElement cutElement,
                                         Integer stitchingAdjustment) {

        this.driver = driver;
        this.logger = logger;
        this.scrollProvider = scrollProvider;
        this.imageProvider = imageProvider;
        this.debugScreenshotsProvider = debugScreenshotsProvider;
        this.scaleProviderFactory = scaleProviderFactory;
        this.cutProvider = cutProvider;
        this.screenshotFactory = screenshotFactory;
        this.waitBeforeScreenshot = waitBeforeScreenshots;
        this.cutElement = cutElement;
        this.stitchingAdjustment = stitchingAdjustment;
    }

    public AppiumFullPageCaptureAlgorithm getAlgorithm () {
        if (EyesAppiumUtils.isAndroid(driver.getRemoteWebDriver())) {
            return new AndroidFullPageCaptureAlgorithm(logger, scrollProvider, imageProvider,
                    debugScreenshotsProvider, scaleProviderFactory, cutProvider, screenshotFactory,
                    waitBeforeScreenshot, stitchingAdjustment);
        } else if (EyesAppiumUtils.isIOS(driver.getRemoteWebDriver())) {
            return new AppiumFullPageCaptureAlgorithm(logger, scrollProvider, imageProvider,
                    debugScreenshotsProvider, scaleProviderFactory, cutProvider, screenshotFactory,
                    waitBeforeScreenshot, cutElement);
        }
        throw new Error("Could not find driver type for getting capture algorithm");
    }

}
