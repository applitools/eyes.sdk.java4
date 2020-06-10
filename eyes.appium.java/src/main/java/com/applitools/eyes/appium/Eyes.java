/*
 * Applitools SDK for Appium integration.
 */
package com.applitools.eyes.appium;

import com.applitools.eyes.*;
import com.applitools.eyes.appium.capture.ImageProviderFactory;
import com.applitools.eyes.appium.locators.VisualLocatorProviderFactory;
import com.applitools.eyes.capture.EyesScreenshotFactory;
import com.applitools.eyes.fluent.ICheckSettings;
import com.applitools.eyes.fluent.ICheckSettingsInternal;
import com.applitools.eyes.locators.IVisualLocatorSettings;
import com.applitools.eyes.positioning.RegionProvider;
import com.applitools.eyes.scaling.FixedScaleProviderFactory;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.ImageUtils;
import io.appium.java_client.AppiumDriver;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;


public class Eyes extends com.applitools.eyes.selenium.Eyes {

    private static final String NATIVE_APP = "NATIVE_APP";
    private EyesAppiumDriver driver;

    private ElementType cutElementType;
    private WebElement cutElement;

    public Eyes() {
        init();
        doNotGetTitle = true;
    }

    @Override
    public String getBaseAgentId() {
        return "eyes.appium.java/4.13.1";
    }

    private void init() {
        // FIXME: 19/06/2018 Not relevant anymore (both the JS handler and treating EyesSeleniumUtils as static)
//        EyesSeleniumUtils.setJavascriptHandler(new AppiumJavascriptHandler(this.driver));
    }

    @Override
    public EyesAppiumDriver getEyesDriver() {
        return driver;
    }

    @Override
    protected void initDriverBasedPositionProviders() {
        logger.verbose("Initializing Appium position provider");
        setPositionProvider(new AppiumScrollPositionProviderFactory(logger, getEyesDriver()).getScrollPositionProvider());
    }

    protected void initImageProvider() {
        imageProvider = ImageProviderFactory.getImageProvider(this, logger, getEyesDriver(), true);
    }


    @Override
    public WebDriver getDriver() {
        return driver;
    }

    @Override
    protected void initDriver(WebDriver driver) {
        if (driver instanceof AppiumDriver) {
            logger.verbose("Found an instance of AppiumDriver, so using EyesAppiumDriver instead");
            this.driver = new EyesAppiumDriver(logger, this, (AppiumDriver) driver);
        } else {
            logger.verbose("Did not find an instance of AppiumDriver, using regular logic");
            /* TODO
               when a breaking change of this library can be published, we can do away with
               this else clause */
            super.initDriver(driver);
        }
    }


    @Override
    protected ScaleProviderFactory getScaleProviderFactory() {
        // in the context of appium, we know the pixel ratio by getting it directly from the appium
        // server, so there's no need to figure anything out on the fly. just return a Fixed one
        return new FixedScaleProviderFactory(1 / getDevicePixelRatio(), scaleProviderHandler);
    }

    /**
     * @param driver The driver for which to check if it represents a mobile
     *               device.
     * @return {@code true} if the platform running the test is a mobile
     * platform. {@code false} otherwise.
     */
    @Override
    protected boolean isMobileDevice(WebDriver driver) {
        return EyesAppiumUtils.isMobileDevice(driver);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This override also checks for mobile operating system.
     */
    @Override
    protected AppEnvironment getAppEnvironment() {

        AppEnvironment appEnv = super.getAppEnvironment();
        RemoteWebDriver underlyingDriver = driver.getRemoteWebDriver();
        // If hostOs isn't set, we'll try and extract and OS ourselves.
        if (appEnv.getOs() == null) {
            logger.log("No OS set, checking for mobile OS...");
            if (EyesAppiumUtils.isMobileDevice(underlyingDriver)) {
                String platformName = null;
                logger.log("Mobile device detected! Checking device type..");
                if (EyesAppiumUtils.isAndroid(underlyingDriver)) {
                    logger.log("Android detected.");
                    platformName = "Android";
                } else if (EyesAppiumUtils.isIOS(underlyingDriver)) {
                    logger.log("iOS detected.");
                    platformName = "iOS";
                } else {
                    logger.log("Unknown device type.");
                }
                // We only set the OS if we identified the device type.
                if (platformName != null) {
                    String os = platformName;
                    String platformVersion = EyesAppiumUtils.getPlatformVersion(underlyingDriver);
                    if (platformVersion != null) {
                        String majorVersion =
                            platformVersion.split("\\.", 2)[0];

                        if (!majorVersion.isEmpty()) {
                            os += " " + majorVersion;
                        }
                    }

                    logger.verbose("Setting OS: " + os);
                    appEnv.setOs(os);
                }
                Object deviceNameCapability = underlyingDriver.getCapabilities().getCapability("deviceName");
                String deviceName = deviceNameCapability != null ? deviceNameCapability.toString() : "Unknown";
                appEnv.setDeviceInfo(deviceName);
            } else {
                logger.log("No mobile OS detected.");
            }
        }
        logger.log("Done!");
        return appEnv;
    }

    @Override
    protected double extractDevicePixelRatio() {
        return getEyesDriver().getDevicePixelRatio();
    }

    private void updateCutElement(EyesAppiumCheckSettings checkSettings) {
        cutElementType = checkSettings.getCutElementType();
        try {
            if (checkSettings.getCutElementSelector() == null) {
                return;
            }
            cutElement = getDriver().findElement(checkSettings.getCutElementSelector());
        } catch (NoSuchElementException ignored) {
            logger.verbose("Element to cut is not found with selector: " + checkSettings.getCutElementSelector());
        }
    }

    @Override
    public void check(ICheckSettings checkSettings) {
        if (checkSettings instanceof EyesAppiumCheckSettings) {
            updateCutElement((EyesAppiumCheckSettings)checkSettings);
        }
        super.check(checkSettings);
    }

    @Override
    protected MatchResult checkElement(WebElement element, String name, ICheckSettings checkSettings) {
        return checkWindowBase(new RegionProvider() {
            @Override
            public Region getRegion() {
                return getElementRegion(element, checkSettings);
            }
        }, name, false, checkSettings);
    }

    protected EyesAppiumScreenshot getFullPageScreenshot() {

        logger.verbose("Full page Appium screenshot requested.");

        EyesScreenshotFactory screenshotFactory = new EyesAppiumScreenshotFactory(logger, getEyesDriver());
        ScaleProviderFactory scaleProviderFactory = updateScalingParams();

        AppiumScrollPositionProvider scrollPositionProvider = (AppiumScrollPositionProvider) getPositionProvider();

        AppiumCaptureAlgorithmFactory algoFactory = new AppiumCaptureAlgorithmFactory(getEyesDriver(), logger,
                scrollPositionProvider, imageProvider, debugScreenshotsProvider, scaleProviderFactory,
                cutProviderHandler.get(), screenshotFactory, getWaitBeforeScreenshots(), cutElement, getStitchOverlap());

        AppiumFullPageCaptureAlgorithm algo = algoFactory.getAlgorithm();

        BufferedImage fullPageImage = algo
            .getStitchedRegion(Region.EMPTY, getStitchOverlap(), regionPositionCompensation);

//        // FIXME: 26/04/2018 Not sure this is the correct way to get the scrollable region (make sure position is not related to content offset)
//        Region scrollableViewRegion = scrollPositionProvider.getScrollableViewRegion();
//        BufferedImage fullPageImage = algo
//            .getStitchedRegion(scrollableViewRegion, getStitchOverlap(), regionPositionCompensation);

        return new EyesAppiumScreenshot(logger, getEyesDriver(), fullPageImage);
    }

    protected EyesAppiumScreenshot getSimpleScreenshot() {
        ScaleProviderFactory scaleProviderFactory = updateScalingParams();
//        ensureElementVisible(this.targetElement);

        logger.verbose("Screenshot requested...");
        BufferedImage screenshotImage = imageProvider.getImage();
        debugScreenshotsProvider.save(screenshotImage, "original");

        ScaleProvider scaleProvider = scaleProviderFactory.getScaleProvider(screenshotImage.getWidth());
        if (scaleProvider.getScaleRatio() != 1.0) {
            logger.verbose("scaling...");
            screenshotImage = ImageUtils.scaleImage(screenshotImage, scaleProvider);
            debugScreenshotsProvider.save(screenshotImage, "scaled");
        }

        CutProvider cutProvider = cutProviderHandler.get();
        if (!(cutProvider instanceof NullCutProvider)) {
            logger.verbose("cutting...");
            screenshotImage = cutProvider.cut(screenshotImage);
            debugScreenshotsProvider.save(screenshotImage, "cut");
        }

        logger.verbose("Creating screenshot object...");
        return new EyesAppiumScreenshot(logger, getEyesDriver(), screenshotImage);
    }

    @Override
    protected MatchResult checkRegion(String name, ICheckSettings checkSettings) {
        MatchResult result = checkWindowBase(new RegionProvider() {
            @Override
            public Region getRegion() {
                Point p = targetElement.getLocation();
                Dimension d = targetElement.getSize();
                return new Region(p.getX(), p.getY(), d.getWidth(), d.getHeight(), CoordinatesType.CONTEXT_RELATIVE);
            }
        }, name, false, checkSettings);
        logger.verbose("Done! trying to scroll back to original position.");

        return result;
    }

    @Override
    protected EyesScreenshot getSubScreenshot(EyesScreenshot screenshot, Region region, ICheckSettingsInternal checkSettingsInternal) {
        ArgumentGuard.notNull(region, "region");
        if ((EyesAppiumUtils.isAndroid(driver) || EyesAppiumUtils.isIOS(driver))
                && region.getCoordinatesType() != CoordinatesType.CONTEXT_RELATIVE) {
            logger.verbose(String.format("getSubScreenshot([%s])", region));

            BufferedImage image = screenshot.getImage();
            if (image.getWidth() < driver.getViewportRect().get("width")) {
                image = ImageUtils.scaleImage(image, driver.getEyes().getDevicePixelRatio());
            }
            BufferedImage subScreenshotImage = ImageUtils.scaleImage(ImageUtils.getImagePart(image, region),
                    1/driver.getEyes().getDevicePixelRatio());

            EyesAppiumScreenshot result = new EyesAppiumScreenshot(logger, driver, subScreenshotImage,
                    new RectangleSize(subScreenshotImage.getWidth(), subScreenshotImage.getHeight()));

            logger.verbose("Done!");
            return result;
        } else {
            return screenshot.getSubScreenshot(region, false);
        }
    }

    @Override
    public Location getElementOriginalLocation(WebElement element) {
        try {
            ContentSize contentSize = EyesAppiumUtils.getContentSize(driver.getRemoteWebDriver(), element);
            return new Location(contentSize.left, contentSize.top);
        } catch (IOException ignored) {
            return new Location(element.getLocation().getX(), element.getLocation().getY());
        }
    }

    @Override
    protected void initVisualLocatorProvider() {
        VisualLocatorProviderFactory factory = new VisualLocatorProviderFactory(driver, logger, serverConnector, debugScreenshotsProvider);
        visualLocatorProvider = factory.getProvider();
    }

    // TODO override implementation of getFrameOrElementScreenshot

    public Map<String, List<Region>> locate(IVisualLocatorSettings visualLocatorSettings) {
        ArgumentGuard.notNull(visualLocatorSettings, "visualLocatorSettings");
        return visualLocatorProvider.getLocators(visualLocatorSettings);
    }

    private Region getElementRegion(WebElement element, ICheckSettings checkSettings) {
        logger.verbose("Get element region...");
        Boolean statusBarExists = null;
        if (checkSettings instanceof SeleniumCheckSettings) {
            statusBarExists = ((SeleniumCheckSettings) checkSettings).getStatusBarExists();
        }
        Region region = ((AppiumScrollPositionProvider) getPositionProvider()).getElementRegion(element, shouldStitchContent(), statusBarExists);
        logger.verbose("Element region: " + region.toString());
        return region;
    }
}
