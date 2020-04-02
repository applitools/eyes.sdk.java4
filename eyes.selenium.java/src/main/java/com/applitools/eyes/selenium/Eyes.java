/*
 * Applitools SDK for Selenium integration.
 */
package com.applitools.eyes.selenium;

import com.applitools.eyes.*;
import com.applitools.eyes.capture.AppOutputWithScreenshot;
import com.applitools.eyes.capture.EyesScreenshotFactory;
import com.applitools.eyes.capture.ImageProvider;
import com.applitools.eyes.diagnostics.TimedAppOutput;
import com.applitools.eyes.events.ValidationInfo;
import com.applitools.eyes.events.ValidationResult;
import com.applitools.eyes.exceptions.TestFailedException;
import com.applitools.eyes.fluent.GetRegion;
import com.applitools.eyes.fluent.ICheckSettings;
import com.applitools.eyes.fluent.ICheckSettingsInternal;
import com.applitools.eyes.fluent.IgnoreRegionByRectangle;
import com.applitools.eyes.positioning.*;
import com.applitools.eyes.scaling.FixedScaleProviderFactory;
import com.applitools.eyes.scaling.NullScaleProvider;
import com.applitools.eyes.selenium.capture.*;
import com.applitools.eyes.selenium.config.Configuration;
import com.applitools.eyes.selenium.exceptions.EyesDriverOperationException;
import com.applitools.eyes.selenium.fluent.*;
import com.applitools.eyes.selenium.frames.Frame;
import com.applitools.eyes.selenium.frames.FrameChain;
import com.applitools.eyes.selenium.positioning.*;
import com.applitools.eyes.selenium.regionVisibility.MoveToRegionVisibilityStrategy;
import com.applitools.eyes.selenium.regionVisibility.NopRegionVisibilityStrategy;
import com.applitools.eyes.selenium.regionVisibility.RegionVisibilityStrategy;
import com.applitools.eyes.selenium.wrappers.EyesRemoteWebElement;
import com.applitools.eyes.selenium.wrappers.EyesTargetLocator;
import com.applitools.eyes.selenium.wrappers.EyesWebDriver;
import com.applitools.eyes.triggers.MouseAction;
import com.applitools.utils.*;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * The main API gateway for the SDK.
 */
@SuppressWarnings("WeakerAccess")
public class Eyes extends EyesBase {

    private FrameChain originalFC;
    private WebElement scrollRootElement;
    private PositionProvider currentFramePositionProvider;

    public PositionProvider getCurrentFramePositionProvider() {
        return currentFramePositionProvider;
    }

    @SuppressWarnings("UnusedDeclaration")
    public interface WebDriverAction {
        void drive(WebDriver driver);
    }

    public static final double UNKNOWN_DEVICE_PIXEL_RATIO = 0;
    public static final double DEFAULT_DEVICE_PIXEL_RATIO = 1;

    private static final int USE_DEFAULT_MATCH_TIMEOUT = -1;

    // Seconds
    private static final int RESPONSE_TIME_DEFAULT_DEADLINE = 10;
    // Seconds
    private static final int RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE = 20;

    // Milliseconds
    private static final int DEFAULT_WAIT_BEFORE_SCREENSHOTS = 100;

    private EyesWebDriver driver;
    protected boolean doNotGetTitle;

    private boolean checkFrameOrElement;

    public Region getRegionToCheck() {
        return regionToCheck;
    }

    public void setRegionToCheck(Region regionToCheck) {
        this.regionToCheck = regionToCheck;
    }

    private Region regionToCheck = null;

    private String originalOverflow;

    private ImageRotation rotation;
    private double devicePixelRatio;
    private PropertyHandler<RegionVisibilityStrategy> regionVisibilityStrategyHandler;
    private ElementPositionProvider elementPositionProvider;
    private SeleniumJavaScriptExecutor jsExecutor;

    private UserAgent userAgent;
    protected ImageProvider imageProvider;
    protected RegionPositionCompensation regionPositionCompensation;
    protected WebElement targetElement = null;
    private PositionMemento positionMemento;
    private Region effectiveViewport = Region.EMPTY;

    private EyesScreenshotFactory screenshotFactory;

    private boolean stitchContent = false;

    protected void ensureConfiguration() {
        config = new Configuration();
    }

    private Configuration getConfig() {
        return (Configuration) config;
    }

    public boolean getHideCaret() {
        return getConfig().getHideCaret();
    }

    public void setHideCaret(boolean hideCaret) {
        getConfig().setHideCaret(hideCaret);
    }

    public boolean shouldStitchContent() {
        return stitchContent;
    }

    /**
     * Creates a new Eyes instance that interacts with the Eyes cloud
     * service.
     */
    public Eyes() {
        checkFrameOrElement = false;
        doNotGetTitle = false;
        devicePixelRatio = UNKNOWN_DEVICE_PIXEL_RATIO;
        regionVisibilityStrategyHandler = new SimplePropertyHandler<>();
        regionVisibilityStrategyHandler.set(new MoveToRegionVisibilityStrategy(logger));
        EyesTargetLocator.initLogger(logger);
    }

    // *** FOR APPIUM
    protected SeleniumJavaScriptExecutor getJsExecutor() {
        return jsExecutor;
    }

    protected void setJsExecutor(SeleniumJavaScriptExecutor jsExecutor) {
        this.jsExecutor = jsExecutor;
    }

    protected UserAgent getUserAgent() {
        return userAgent;
    }

    protected void setUserAgent(UserAgent userAgent) {
        this.userAgent = userAgent;
    }
    // *****


    @Override
    public String getBaseAgentId() {
        return "eyes.selenium.java/4.7.0";
    }

    public EyesWebDriver getEyesDriver() {
        return driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    /**
     * ﻿Forces a full page screenshot (by scrolling and stitching) if the
     * browser only ﻿supports viewport screenshots).
     * @param shouldForce Whether to force a full page screenshot or not.
     */
    public void setForceFullPageScreenshot(boolean shouldForce) {
        getConfig().setForceFullPageScreenshot(shouldForce);
    }

    /**
     * @return Whether Eyes should force a full page screenshot.
     */
    public boolean getForceFullPageScreenshot() {
        return getConfig().getForceFullPageScreenshot();
    }

    /**
     * Sets the time to wait just before taking a screenshot (e.g., to allow
     * positioning to stabilize when performing a full page stitching).
     * @param waitBeforeScreenshots The time to wait (Milliseconds). Values
     *                              smaller or equal to 0, will cause the
     *                              default value to be used.
     */
    public void setWaitBeforeScreenshots(int waitBeforeScreenshots) {
        getConfig().setWaitBeforeScreenshots(waitBeforeScreenshots);
    }

    /**
     * @return The time to wait just before taking a screenshot.
     */
    public int getWaitBeforeScreenshots() {
        return getConfig().getWaitBeforeScreenshots();
    }

    /**
     * Turns on/off the automatic scrolling to a region being checked by
     * {@code checkRegion}.
     * @param shouldScroll Whether to automatically scroll to a region being validated.
     */
    public void setScrollToRegion(boolean shouldScroll) {
        if (shouldScroll) {
            regionVisibilityStrategyHandler = new ReadOnlyPropertyHandler<RegionVisibilityStrategy>(logger, new MoveToRegionVisibilityStrategy(logger));
        } else {
            regionVisibilityStrategyHandler = new ReadOnlyPropertyHandler<RegionVisibilityStrategy>(logger, new NopRegionVisibilityStrategy(logger));
        }
    }

    /**
     * @return Whether to automatically scroll to a region being validated.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean getScrollToRegion() {
        return !(regionVisibilityStrategyHandler.get() instanceof NopRegionVisibilityStrategy);
    }

    /**
     * Set the type of stitching used for full page screenshots. When the
     * page includes fixed position header/sidebar, use {@link StitchMode#CSS}.
     * Default is {@link StitchMode#SCROLL}.
     * @param mode The stitch mode to set.
     */
    public void setStitchMode(StitchMode mode) {
        logger.verbose("setting stitch mode to " + mode);
        getConfig().setStitchMode(mode);
        if (getEyesDriver() != null) {
            positionProviderHandler.set(createPositionProvider());
        }
    }

    /**
     * @return The current stitch mode settings.
     */
    public StitchMode getStitchMode() {
        return getConfig().getStitchMode();
    }

    /**
     * Hide the scrollbars when taking screenshots.
     * @param shouldHide Whether to hide the scrollbars or not.
     */
    public void setHideScrollbars(boolean shouldHide) {
        getConfig().setHideScrollbars(shouldHide);
    }

    /**
     * @return Whether or not scrollbars are hidden when taking screenshots.
     */
    public boolean getHideScrollbars() {
        return getConfig().getHideScrollbars();
    }

    /**
     * @return The image rotation data.
     */
    public ImageRotation getRotation() {
        return rotation;
    }

    /**
     * @param rotation The image rotation data.
     */
    public void setRotation(ImageRotation rotation) {
        this.rotation = rotation;
        if (getEyesDriver() != null) {
            getEyesDriver().setRotation(rotation);
        }
    }

    /**
     * @return The device pixel ratio, or {@link #UNKNOWN_DEVICE_PIXEL_RATIO}
     * if the DPR is not known yet or if it wasn't possible to extract it.
     */
    public double getDevicePixelRatio() {
        return devicePixelRatio;
    }

    public WebDriver open(WebDriver driver, Configuration configuration) {
        config = configuration;
        return open(driver);
    }

    /**
     * See {@link #open(WebDriver, String, String, RectangleSize, SessionType)}.
     * {@code sessionType} defaults to {@code null}.
     */
    public WebDriver open(WebDriver driver, String appName, String testName,
                          RectangleSize viewportSize) {
        config.setAppName(appName);
        config.setTestName(testName);
        config.setViewportSize(viewportSize);
        return open(driver);
    }

    /**
     * See {@link #open(WebDriver, String, String, SessionType)}.
     * {@code viewportSize} defaults to {@code null}.
     * {@code sessionType} defaults to {@code null}.
     */
    public WebDriver open(WebDriver driver, String appName, String testName) {
        config.setAppName(appName);
        config.setTestName(testName);
        return open(driver);
    }


    /**
     * Starts a test.
     * @param driver       The web driver that controls the browser hosting
     *                     the application under test.
     * @param appName      The name of the application under test.
     * @param testName     The test name.
     * @param viewportSize The required browser's viewport size
     *                     (i.e., the visible part of the document's body) or
     *                     {@code null} to use the current window's viewport.
     * @param sessionType  The type of test (e.g.,  standard test / visual
     *                     performance test).
     * @return A wrapped WebDriver which enables Eyes trigger recording and
     * frame handling.
     */
    protected WebDriver open(WebDriver driver, String appName, String testName,
                             RectangleSize viewportSize, SessionType sessionType) {
        config.setAppName(appName);
        config.setTestName(testName);
        config.setViewportSize(viewportSize);
        config.setSessionType(sessionType);
        return open(driver);
    }

    protected void initImageProvider() {
        imageProvider = ImageProviderFactory.getImageProvider(getUserAgent(), this, logger, getEyesDriver());
    }

    protected void initDriverBasedPositionProviders() { }

    protected WebDriver open(WebDriver driver) {
        if (getIsDisabled()) {
            logger.verbose("Ignored");
            return driver;
        }

        ArgumentGuard.notNull(driver, "driver");

        initDriver(driver);

        tryUpdateDevicePixelRatio();

        screenshotFactory = new EyesWebDriverScreenshotFactory(logger, getEyesDriver());

        ensureViewportSize();

        openBase();

        setUserAgent(UserAgent.ParseUserAgentString(sessionStartInfo.getEnvironment().getInferred(), true));

        initImageProvider();
        regionPositionCompensation = RegionPositionCompensationFactory.getRegionPositionCompensation(getUserAgent(),
                this, logger);

        initDriverBasedPositionProviders();

        setJsExecutor(new SeleniumJavaScriptExecutor(getEyesDriver()));

        getEyesDriver().setRotation(getRotation());
        return getEyesDriver();
    }

    private void ensureViewportSize() {
        if (this.config.getViewportSize() == null) {
            this.config.setViewportSize(getEyesDriver().getDefaultContentViewportSize());
        }
    }

    protected void initDriver(WebDriver driver) {
        if (driver instanceof RemoteWebDriver) {
            this.driver = new EyesWebDriver(logger, this, (RemoteWebDriver) driver);
        } else if (driver instanceof EyesWebDriver) {
            this.driver = (EyesWebDriver) driver;
        } else {
            String errMsg = "Driver is not a RemoteWebDriver (" +
                    driver.getClass().getName() + ")";
            logger.log(errMsg);
            throw new EyesException(errMsg);
        }
        if (isMobileDevice(driver)) {
            regionVisibilityStrategyHandler.set(new NopRegionVisibilityStrategy(logger));
        }
    }

    private PositionProvider createPositionProvider() {
        return createPositionProvider(scrollRootElement);
    }

    private PositionProvider createPositionProvider(WebElement scrollRootElement) {
        // Setting the correct position provider.
        StitchMode stitchMode = getStitchMode();
        logger.verbose("initializing position provider. stitchMode: " + stitchMode);
        switch (stitchMode) {
            case CSS:
                return new CssTranslatePositionProvider(logger, getJsExecutor(), scrollRootElement);
            default:
                return new ScrollPositionProvider(logger, getJsExecutor());
        }
    }

    /**
     * See {@link #open(WebDriver, String, String, RectangleSize)}.
     * {@code viewportSize} defaults to {@code null}.
     */
    protected WebDriver open(WebDriver driver, String appName, String testName, SessionType sessionType) {
        return open(driver, appName, testName, null, sessionType);
    }

    /**
     * See {@link #checkWindow(String)}.
     * {@code tag} defaults to {@code null}.
     * Default match timeout is used.
     */
    public void checkWindow() {
        checkWindow(null);
    }

    /**
     * See {@link #checkWindow(int, String)}.
     * Default match timeout is used.
     * @param tag An optional tag to be associated with the snapshot.
     */
    public void checkWindow(String tag) {
        check(tag, Target.window());
    }

    /**
     * Takes a snapshot of the application under test and matches it with
     * the expected output.
     * @param matchTimeout The amount of time to retry matching (Milliseconds).
     * @param tag          An optional tag to be associated with the snapshot.
     * @throws TestFailedException Thrown if a mismatch is detected and
     *                             immediate failure reports are enabled.
     */
    public void checkWindow(int matchTimeout, String tag) {
        check(tag, Target.window().timeout(matchTimeout));
    }

    /**
     * Runs a test on the current window.
     * @param driver       The web driver that controls the browser hosting
     *                     the application under test.
     * @param appName      The name of the application under test.
     * @param testName     The test name (will also be used as the tag name for the step).
     * @param viewportSize The required browser's viewport size
     *                     (i.e., the visible part of the document's body) or
     *                     {@code null} to use the current window's viewport.
     */
    public void testWindow(WebDriver driver, String appName, String testName,
                           RectangleSize viewportSize) {
        open(driver, appName, testName, viewportSize);
        try {
            checkWindow(testName);
            close();
        } finally {
            abortIfNotClosed();
        }
    }

    /**
     * See {@link #testWindow(WebDriver, String, String, RectangleSize)}.
     * {@code viewportSize} defaults to {@code null}.
     */
    public void testWindow(WebDriver driver, String appName, String testName) {
        testWindow(driver, appName, testName, null);
    }

    /**
     * See {@link #testWindow(WebDriver, String, String, RectangleSize)}.
     * {@code appName} defaults to {@code null} (which means the name set in
     * {@link #setAppName(String)} would be used.
     */
    public void testWindow(WebDriver driver, String testName,
                           RectangleSize viewportSize) {
        testWindow(driver, null, testName, viewportSize);
    }

    /**
     * See {@link #testWindow(WebDriver, String, RectangleSize)}.
     * {@code viewportSize} defaults to {@code null}.
     */
    public void testWindow(WebDriver driver, String testName) {
        testWindow(driver, testName, (RectangleSize) null);
    }

    /**
     * Run a visual performance test.
     * @param driver   The driver to use.
     * @param appName  The name of the application being tested.
     * @param testName The test name.
     * @param action   Actions to be performed in parallel to starting the test.
     * @param deadline The expected time until the application should have been loaded. (Seconds)
     * @param timeout  The maximum time until the application should have been loaded. (Seconds)
     */
    public void testResponseTime(final WebDriver driver, String appName,
                                 String testName, final WebDriverAction action,
                                 int deadline, int timeout) {
        open(driver, appName, testName, SessionType.PROGRESSION);
        Runnable runnableAction = null;
        if (action != null) {
            runnableAction = new Runnable() {
                public void run() {
                    action.drive(driver);
                }
            };
        }

        MatchWindowDataWithScreenshot result =
                super.testResponseTimeBase(NullRegionProvider.INSTANCE,
                        runnableAction,
                        deadline,
                        timeout,
                        5000);

        logger.verbose("Checking if deadline was exceeded...");
        boolean deadlineExceeded = true;
        if (result != null) {
            TimedAppOutput tao =
                    (TimedAppOutput) result.getMatchWindowData().getAppOutput();
            long resultElapsed = tao.getElapsed();
            long deadlineMs = deadline * 1000;
            logger.verbose(String.format(
                    "Deadline: %d, Elapsed time for match: %d",
                    deadlineMs, resultElapsed));
            deadlineExceeded = resultElapsed > deadlineMs;
        }
        logger.verbose("Deadline exceeded? " + deadlineExceeded);

        closeResponseTime(deadlineExceeded);
    }

    /**
     * See {@link #testResponseTime(WebDriver, String, String, WebDriverAction, int, int)}.
     * {@code timeout} defaults to {@code deadline} + {@link #RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE}.
     */
    public void testResponseTime(WebDriver driver, String appName,
                                 String testName, WebDriverAction action,
                                 int deadline) {
        testResponseTime(driver, appName, testName, action, deadline,
                (deadline + RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE));
    }

    /**
     * See {@link #testResponseTime(WebDriver, String, String, WebDriverAction, int, int)}.
     * {@code deadline} defaults to {@link #RESPONSE_TIME_DEFAULT_DEADLINE}.
     * {@code timeout} defaults to {@link #RESPONSE_TIME_DEFAULT_DEADLINE} + {@link #RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE}.
     */
    public void testResponseTime(WebDriver driver, String appName,
                                 String testName, WebDriverAction action) {
        testResponseTime(driver, appName, testName, action,
                RESPONSE_TIME_DEFAULT_DEADLINE,
                (RESPONSE_TIME_DEFAULT_DEADLINE +
                        RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE));
    }

    /**
     * See {@link #testResponseTime(WebDriver, String, String, WebDriverAction, int, int)}.
     * {@code action} defaults to {@code null}.
     * {@code timeout} defaults to {@code deadline} + {@link #RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE}.
     */
    public void testResponseTime(WebDriver driver, String appName,
                                 String testName, int deadline) {
        testResponseTime(driver, appName, testName, null, deadline,
                (deadline + RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE));
    }

    /**
     * See {@link #testResponseTime(WebDriver, String, String, WebDriverAction, int, int)}.
     * {@code deadline} defaults to {@link #RESPONSE_TIME_DEFAULT_DEADLINE}.
     * {@code timeout} defaults to {@link #RESPONSE_TIME_DEFAULT_DEADLINE} + {@link #RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE}.
     * {@code action} defaults to {@code null}.
     */
    public void testResponseTime(WebDriver driver, String appName,
                                 String testName) {
        testResponseTime(driver, appName, testName, null,
                RESPONSE_TIME_DEFAULT_DEADLINE,
                (RESPONSE_TIME_DEFAULT_DEADLINE +
                        RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE));
    }

    /**
     * Similar to {@link #testResponseTime(WebDriver, String, String, WebDriverAction, int, int)},
     * except this method sets the viewport size before starting the
     * performance test.
     * @param viewportSize The required viewport size.
     */
    public void testResponseTime(WebDriver driver, String appName,
                                 String testName, WebDriverAction action,
                                 int deadline, int timeout,
                                 RectangleSize viewportSize) {
        // Notice we specifically use the setViewportSize overload which does
        // not handle frames (as we want to make sure this is as fast as
        // possible).
        setViewportSize(driver, viewportSize);

        testResponseTime(driver, appName, testName, action, deadline, timeout);
    }

    /**
     * See {@link #testResponseTime(WebDriver, String, String, WebDriverAction, int, int, RectangleSize)}.
     * {@code timeout} defaults to {@code deadline} + {@link #RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE}.
     */
    public void testResponseTime(WebDriver driver, String appName,
                                 String testName, WebDriverAction action,
                                 int deadline, RectangleSize viewportSize) {
        testResponseTime(driver, appName, testName, action, deadline,
                (deadline + RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE),
                viewportSize);
    }

    /**
     * See {@link #testResponseTime(WebDriver, String, String, WebDriverAction, int, int, RectangleSize)}.
     * {@code deadline} defaults to {@link #RESPONSE_TIME_DEFAULT_DEADLINE}.
     * {@code timeout} defaults to {@link #RESPONSE_TIME_DEFAULT_DEADLINE} + {@link #RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE}.
     */
    public void testResponseTime(WebDriver driver, String appName,
                                 String testName, WebDriverAction action,
                                 RectangleSize viewportSize) {
        testResponseTime(driver, appName, testName, action,
                RESPONSE_TIME_DEFAULT_DEADLINE,
                (RESPONSE_TIME_DEFAULT_DEADLINE +
                        RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE),
                viewportSize);
    }

    /**
     * See {@link #testResponseTime(WebDriver, String, String, WebDriverAction, int, int, RectangleSize)}.
     * {@code action} defaults to {@code null}.
     */
    public void testResponseTime(WebDriver driver, String appName,
                                 String testName, int deadline, int timeout,
                                 RectangleSize viewportSize) {
        testResponseTime(driver, appName, testName, null, deadline, timeout,
                viewportSize);
    }

    /**
     * See {@link #testResponseTime(WebDriver, String, String, int, int, RectangleSize)}.
     * {@code timeout} defaults to {@code deadline} + {@link #RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE}.
     */
    public void testResponseTime(WebDriver driver, String appName,
                                 String testName, int deadline,
                                 RectangleSize viewportSize) {
        testResponseTime(driver, appName, testName, deadline,
                (deadline + RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE),
                viewportSize);
    }

    /**
     * See {@link #testResponseTime(WebDriver, String, String, int, int, RectangleSize)}.
     * {@code deadline} defaults to {@link #RESPONSE_TIME_DEFAULT_DEADLINE}.
     * {@code timeout} defaults to {@link #RESPONSE_TIME_DEFAULT_DEADLINE} + {@link #RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE}.
     */
    public void testResponseTime(WebDriver driver, String appName,
                                 String testName, RectangleSize viewportSize) {
        testResponseTime(driver, appName, testName,
                RESPONSE_TIME_DEFAULT_DEADLINE,
                (RESPONSE_TIME_DEFAULT_DEADLINE +
                        RESPONSE_TIME_DEFAULT_DIFF_FROM_DEADLINE),
                viewportSize);
    }

    /**
     * Takes multiple screenshots at once (given all <code>ICheckSettings</code> objects are on the same level).
     * @param checkSettings Multiple <code>ICheckSettings</code> object representing different regions in the viewport.
     */
    public void check(ICheckSettings... checkSettings) {
        if (getIsDisabled()) {
            logger.log(String.format("check(ICheckSettings[%d]): Ignored", checkSettings.length));
            return;
        }

        boolean originalForceFPS = getConfig().getForceFullPageScreenshot();

        if (checkSettings.length > 1) {
            getConfig().setForceFullPageScreenshot(true);
        }

        Dictionary<Integer, GetRegion> getRegions = new Hashtable<>();
        Dictionary<Integer, ICheckSettingsInternal> checkSettingsInternalDictionary = new Hashtable<>();

        for (int i = 0; i < checkSettings.length; ++i) {
            ICheckSettings settings = checkSettings[i];
            ICheckSettingsInternal checkSettingsInternal = (ICheckSettingsInternal) settings;

            checkSettingsInternalDictionary.put(i, checkSettingsInternal);

            Region targetRegion = checkSettingsInternal.getTargetRegion();

            if (targetRegion != null) {
                getRegions.put(i, new IgnoreRegionByRectangle(targetRegion));
            } else {
                ISeleniumCheckTarget seleniumCheckTarget =
                        (settings instanceof ISeleniumCheckTarget) ? (ISeleniumCheckTarget) settings : null;

                if (seleniumCheckTarget != null) {
                    WebElement targetElement = getTargetElement(seleniumCheckTarget);
                    if (targetElement == null && seleniumCheckTarget.getFrameChain().size() == 1) {
                        targetElement = getFrameElement(seleniumCheckTarget.getFrameChain().get(0));
                    }

                    if (targetElement != null) {
                        getRegions.put(i, new SimpleRegionByElement(targetElement));
                    }
                }
            }
            //check(settings);
        }

        this.scrollRootElement = getEyesDriver().findElement(By.tagName("html"));
        this.currentFramePositionProvider = null;

        matchRegions(getRegions, checkSettingsInternalDictionary, checkSettings);
        getConfig().setForceFullPageScreenshot(originalForceFPS);
    }

    private void matchRegions(Dictionary<Integer, GetRegion> getRegions,
                              Dictionary<Integer, ICheckSettingsInternal> checkSettingsInternalDictionary,
                              ICheckSettings[] checkSettings) {

        if (getRegions.size() == 0) return;

        this.originalFC = getEyesDriver().getFrameChain().clone();

        FrameChain originalFC = tryHideScrollbars();

        Region bBox = findBoundingBox(getRegions, checkSettings);

        MatchWindowTask mwt = new MatchWindowTask(logger, serverConnector, runningSession, getMatchTimeout(), this);

        ScaleProviderFactory scaleProviderFactory = updateScalingParams();
        FullPageCaptureAlgorithm algo = createFullPageCaptureAlgorithm(scaleProviderFactory);

        BufferedImage screenshotImage = algo.getStitchedRegion(
                Region.EMPTY,
                bBox, positionProviderHandler.get());

        debugScreenshotsProvider.save(screenshotImage, "original");
        EyesWebDriverScreenshot screenshot = new EyesWebDriverScreenshot(logger, getEyesDriver(), screenshotImage, null, bBox.getNegativeLocation());

        for (int i = 0; i < checkSettings.length; ++i) {
            if (((Hashtable<Integer, GetRegion>) getRegions).containsKey(i)) {
                GetRegion getRegion = getRegions.get(i);
                ICheckSettingsInternal checkSettingsInternal = checkSettingsInternalDictionary.get(i);
                List<EyesScreenshot> subScreenshots = getSubScreenshots(screenshot, getRegion);
                matchRegion(checkSettingsInternal, mwt, subScreenshots);
            }
        }

        tryRestoreScrollbars(originalFC);
        ((EyesTargetLocator) getEyesDriver().switchTo()).frames(this.originalFC);
    }

    private List<EyesScreenshot> getSubScreenshots(EyesWebDriverScreenshot screenshot, GetRegion getRegion) {
        List<EyesScreenshot> subScreenshots = new ArrayList<>();
        for (Region r : getRegion.getRegions(this, screenshot, true)) {
            logger.verbose("original sub-region: " + r);
            r = regionPositionCompensation.compensateRegionPosition(r, getDevicePixelRatio());
            logger.verbose("sub-region after compensation: " + r);
            EyesScreenshot subScreenshot = screenshot.getSubScreenshotForRegion(r, false);
            subScreenshots.add(subScreenshot);
        }
        return subScreenshots;
    }

    private void matchRegion(ICheckSettingsInternal checkSettingsInternal, MatchWindowTask mwt, List<EyesScreenshot> subScreenshots) {

        String name = checkSettingsInternal.getName();
        for (EyesScreenshot subScreenshot : subScreenshots) {

            debugScreenshotsProvider.save(subScreenshot.getImage(), String.format("subscreenshot_%s", name));

            ImageMatchSettings ims = mwt.createImageMatchSettings(checkSettingsInternal, subScreenshot);

            BufferedImage screenshotImage = subScreenshot.getImage();
            byte[] screenshotBytes = ImageUtils.encodeAsPng(screenshotImage);

            AppOutput appOutput = new AppOutput(name, screenshotBytes, null, null);
            AppOutputWithScreenshot appOutputWithScreenshot = new AppOutputWithScreenshot(appOutput, subScreenshot);
            MatchResult matchResult = mwt.performMatch(
                    new Trigger[0], appOutputWithScreenshot, name, false, ims);

            logger.verbose("matchResult.asExcepted: " + matchResult.getAsExpected());
        }
    }

    private Region findBoundingBox(Dictionary<Integer, GetRegion> getRegions, ICheckSettings[] checkSettings) {
        RectangleSize rectSize = getViewportSize();

        EyesScreenshot screenshot = new EyesWebDriverScreenshot(logger, getEyesDriver(),
                new BufferedImage(rectSize.getWidth(), rectSize.getHeight(), BufferedImage.TYPE_INT_RGB));

        return findBoundingBox(getRegions, checkSettings, screenshot);
    }

    private Region findBoundingBox(Dictionary<Integer, GetRegion> getRegions, ICheckSettings[] checkSettings, EyesScreenshot screenshot) {
        Region bBox = null;
        for (int i = 0; i < checkSettings.length; ++i) {
            GetRegion getRegion = getRegions.get(i);
            if (getRegion != null) {
                List<Region> regions = getRegion.getRegions(this, screenshot, true);
                for (Region region : regions) {
                    if (bBox == null) {
                        bBox = new Region(region);
                    } else {
                        bBox = bBox.expandToContain(region);
                    }
                }
            }
        }
        return bBox;
    }

    private WebElement getFrameElement(FrameLocator frameLocator) {
        WebElement frameReference = frameLocator.getFrameReference();

        if (frameReference == null) {
            By selector = frameLocator.getFrameSelector();
            List<WebElement> possibleFrames = null;
            if (selector != null) {
                possibleFrames = getEyesDriver().findElements(selector);
            } else {
                String nameOrId = frameLocator.getFrameNameOrId();
                if (nameOrId != null) {
                    possibleFrames = getEyesDriver().findElementsById(nameOrId);
                    if (possibleFrames.size() == 0) {
                        possibleFrames = getEyesDriver().findElementsByName(nameOrId);
                        if (possibleFrames.size() == 0) {
                            Integer frameIndex = frameLocator.getFrameIndex();
                            if (frameIndex != null) {
                                possibleFrames = getEyesDriver().findElements(By.cssSelector(String.format("iframe:nth-of-type(%d)", frameIndex)));
                            }
                        }
                    }
                }
            }
            if (possibleFrames != null && possibleFrames.size() > 0) {
                frameReference = possibleFrames.get(0);
            }
        }
        return frameReference;
    }

    private WebElement getTargetElement(ISeleniumCheckTarget seleniumCheckTarget) {
        assert seleniumCheckTarget != null;
        By targetSelector = seleniumCheckTarget.getTargetSelector();
        WebElement targetElement = seleniumCheckTarget.getTargetElement();
        if (targetElement == null && targetSelector != null) {
            targetElement = this.getEyesDriver().findElement(targetSelector);
        }
        return targetElement;
    }

    public void check(String name, ICheckSettings checkSettings) {
        if (getIsDisabled()) {
            logger.log(String.format("check('%s', %s): Ignored", name, checkSettings));
            return;
        }

        ArgumentGuard.notNull(checkSettings, "checkSettings");
        checkSettings = checkSettings.withName(name);
        this.check(checkSettings);
    }

    @Override
    public String tryCaptureDom() {
        ElementPositionProvider positionProvider = new ElementPositionProvider(logger, getEyesDriver(), scrollRootElement);
        DomCapture domCapture = new DomCapture(this);
        String fullWindowDom = domCapture.getFullWindowDom(getEyesDriver(), positionProvider);
        if (this.domCaptureListener != null) {
            this.domCaptureListener.onDomCaptureComplete(fullWindowDom);
        }
        return fullWindowDom;
    }


    public void check(ICheckSettings checkSettings) {
        if (getIsDisabled()) {
            logger.log(String.format("check(%s): Ignored", checkSettings));
            return;
        }

        ArgumentGuard.notNull(checkSettings, "checkSettings");

        ICheckSettingsInternal checkSettingsInternal = (ICheckSettingsInternal) checkSettings;
        ISeleniumCheckTarget seleniumCheckTarget = (checkSettings instanceof ISeleniumCheckTarget) ? (ISeleniumCheckTarget) checkSettings : null;
        String name = checkSettingsInternal.getName();

        this.scrollRootElement = this.getScrollRootElement(seleniumCheckTarget);

        currentFramePositionProvider = null;
        positionProviderHandler.set(createPositionProvider());

        logger.verbose(String.format("check(\"%s\", checkSettings) - begin", name));

        ValidationInfo validationInfo = this.fireValidationWillStartEvent(name);

        if (!isMobileDevice(getEyesDriver())) {
            logger.verbose("URL: " + getEyesDriver().getCurrentUrl());
        }

        this.stitchContent = checkSettingsInternal.getStitchContent();

        final Region targetRegion = checkSettingsInternal.getTargetRegion();

        this.originalFC = getEyesDriver().getFrameChain().clone();

        int switchedToFrameCount = this.switchToFrame(seleniumCheckTarget);

        this.regionToCheck = null;

        MatchResult result = null;

        EyesTargetLocator switchTo = (EyesTargetLocator) getEyesDriver().switchTo();
        FrameChain originalFC = null;
        if (targetRegion != null) {
            logger.verbose("have target region");
            originalFC = tryHideScrollbars();
            result = this.checkWindowBase(new RegionProvider() {
                @Override
                public Region getRegion() {
                    return new Region(targetRegion.getLocation(), targetRegion.getSize(), CoordinatesType.CONTEXT_RELATIVE);
                }
            }, name, false, checkSettings);
        } else if (seleniumCheckTarget != null) {
            WebElement targetElement = getTargetElement(seleniumCheckTarget);
            if (targetElement != null) {
                logger.verbose("have target element");
                originalFC = tryHideScrollbars();
                this.targetElement = targetElement;
                if (this.stitchContent) {
                    result = this.checkElement(name, checkSettings);
                } else {
                    result = this.checkRegion(name, checkSettings);
                }
                this.targetElement = null;
            } else if (seleniumCheckTarget.getFrameChain().size() > 0) {
                logger.verbose("have frame chain");
                originalFC = tryHideScrollbars();
                if (this.stitchContent) {
                    result = this.checkFullFrameOrElement(name, checkSettings);
                } else {
                    result = this.checkFrameFluent(name, checkSettings);
                }
            } else {
                logger.verbose("default case");
                if (!isMobileDevice(getEyesDriver())) {
                    // required to prevent cut line on the last stitched part of the page on some browsers (like firefox).
                    switchTo.defaultContent();
                    originalFC = tryHideScrollbars();
                    currentFramePositionProvider = createPositionProvider(getEyesDriver().findElement(By.tagName("html")));
                }
                result = this.checkWindowBase(NullRegionProvider.INSTANCE, name, false, checkSettings);
                if (!isMobileDevice(getEyesDriver())) {
                    switchTo.frames(this.originalFC);
                }
            }
        }

        if (result == null) {
            result = new MatchResult();
        }

        while (switchedToFrameCount > 0) {
            getEyesDriver().switchTo().parentFrame();
            switchedToFrameCount--;
        }

        if (this.positionMemento != null) {
            this.positionProviderHandler.get().restoreState(this.positionMemento);
            this.positionMemento = null;
        }

        if (!isMobileDevice(getEyesDriver())) {
            switchTo.resetScroll();

            if (originalFC != null) {
                tryRestoreScrollbars(originalFC);
            }

            trySwitchToFrames(getEyesDriver(), switchTo, this.originalFC);
        }

        this.stitchContent = false;

        ValidationResult validationResult = new ValidationResult();
        validationResult.setAsExpected(result.getAsExpected());
        getSessionEventHandlers().validationEnded(getAUTSessionId(), validationInfo.getValidationId(), validationResult);

        logger.verbose("check - done!");
    }

    protected MatchResult checkFrameFluent(String name, ICheckSettings checkSettings) {
        FrameChain frameChain = getEyesDriver().getFrameChain().clone();
        Frame targetFrame = frameChain.pop();
        this.targetElement = targetFrame.getReference();

        EyesTargetLocator switchTo = (EyesTargetLocator) getEyesDriver().switchTo();
        switchTo.framesDoScroll(frameChain);

        MatchResult result = this.checkRegion(name, checkSettings);

        this.targetElement = null;
        return result;
    }

    private int switchToFrame(ISeleniumCheckTarget checkTarget) {
        if (checkTarget == null) {
            return 0;
        }

        List<FrameLocator> frameChain = checkTarget.getFrameChain();
        int switchedToFrameCount = 0;
        for (FrameLocator frameLocator : frameChain) {
            if (switchToFrame(frameLocator)) {
                switchedToFrameCount++;
            }
        }
        return switchedToFrameCount;
    }

    private boolean switchToFrame(ISeleniumFrameCheckTarget frameTarget) {
        WebDriver.TargetLocator switchTo = getEyesDriver().switchTo();

        if (frameTarget.getFrameIndex() != null) {
            switchTo.frame(frameTarget.getFrameIndex());
            return true;
        }

        if (frameTarget.getFrameNameOrId() != null) {
            switchTo.frame(frameTarget.getFrameNameOrId());
            return true;
        }

        if (frameTarget.getFrameReference() != null) {
            WebElement frameElement = frameTarget.getFrameReference();
            if (frameElement != null) {
                switchTo.frame(frameElement);
                return true;
            }
        }

        if (frameTarget.getFrameSelector() != null) {
            WebElement frameElement = getEyesDriver().findElement(frameTarget.getFrameSelector());
            if (frameElement != null) {
                switchTo.frame(frameElement);
                return true;
            }
        }

        return false;
    }

    private MatchResult checkFullFrameOrElement(String name, ICheckSettings checkSettings) {
        checkFrameOrElement = true;

        logger.verbose("enter");

        MatchResult result = checkWindowBase(new RegionProvider() {
            @Override
            public Region getRegion() {
                return getFullFrameOrElementRegion();
            }
        }, name, false, checkSettings);

        checkFrameOrElement = false;
        return result;
    }

    private Region getFullFrameOrElementRegion() {
        logger.verbose("checkFrameOrElement: " + checkFrameOrElement);
        if (checkFrameOrElement) {

            FrameChain fc = ensureFrameVisible();

            // FIXME - Scaling should be handled in a single place instead
            ScaleProviderFactory scaleProviderFactory = updateScalingParams();

            BufferedImage screenshotImage = imageProvider.getImage();

            debugScreenshotsProvider.save(screenshotImage, "checkFullFrameOrElement");

            scaleProviderFactory.getScaleProvider(screenshotImage.getWidth());

            EyesTargetLocator switchTo = (EyesTargetLocator) getEyesDriver().switchTo();
            switchTo.frames(fc);

            final EyesWebDriverScreenshot screenshot = new EyesWebDriverScreenshot(logger, getEyesDriver(), screenshotImage);

            logger.verbose("replacing regionToCheck");
            setRegionToCheck(screenshot.getFrameWindow());
        }

        return Region.EMPTY;
    }

    private FrameChain ensureFrameVisible() {
        logger.verbose("scrollRootElement_: " + scrollRootElement);
        FrameChain originalFC = getEyesDriver().getFrameChain().clone();
        FrameChain fc = getEyesDriver().getFrameChain().clone();
        while (fc.size() > 0) {
            logger.verbose("fc.Count: " + fc.size());
            //getEyesDriver().getRemoteWebDriver().switchTo().parentFrame();
            EyesTargetLocator.parentFrame(getEyesDriver().getRemoteWebDriver().switchTo(), fc);
            Frame prevFrame = fc.pop();
            Frame frame = fc.peek();
            WebElement scrollRootElement = null;
            if (fc.size() == this.originalFC.size()) {
                logger.verbose("PositionProvider: " + positionProviderHandler.get());
                positionMemento = positionProviderHandler.get().getState();
                scrollRootElement = this.scrollRootElement;
            } else {
                if (frame != null) {
                    scrollRootElement = frame.getScrollRootElement();
                }
                if (scrollRootElement == null) {
                    scrollRootElement = getEyesDriver().findElement(By.tagName("html"));
                }
            }
            logger.verbose("scrollRootElement: " + scrollRootElement);

            PositionProvider positionProvider = getElementPositionProvider(scrollRootElement);
            positionProvider.setPosition(prevFrame.getLocation());

            Region reg = new Region(Location.ZERO, prevFrame.getInnerSize());
            effectiveViewport.intersect(reg);
        }
        ((EyesTargetLocator) getEyesDriver().switchTo()).frames(originalFC);
        return originalFC;
    }

    private void ensureElementVisible(WebElement element) {
        if (this.targetElement == null || !getScrollToRegion()) {
            // No element? we must be checking the window.
            return;
        }

        if (isMobileDevice(getEyesDriver().getRemoteWebDriver())) {
            logger.log("NATIVE context identified, skipping 'ensure element visible'");
            return;
        }

        FrameChain originalFC = getEyesDriver().getFrameChain().clone();
        EyesTargetLocator switchTo = (EyesTargetLocator) getEyesDriver().switchTo();

        EyesRemoteWebElement eyesRemoteWebElement = new EyesRemoteWebElement(logger, getEyesDriver(), element);
        Region elementBounds = eyesRemoteWebElement.getBounds();

        Location currentFrameOffset = originalFC.getCurrentFrameOffset();
        elementBounds = elementBounds.offset(currentFrameOffset.getX(), currentFrameOffset.getY());

        Region viewportBounds = getViewportScrollBounds();

        logger.verbose("viewportBounds: " + viewportBounds + " ; elementBounds: " + elementBounds);

        if (!viewportBounds.contains(elementBounds)) {
            ensureFrameVisible();

            Point p = element.getLocation();
            Location elementLocation = new Location(p.getX(), p.getY());

            WebElement scrollRootElement;
            if (originalFC.size() > 0 && !element.equals(originalFC.peek().getReference())) {
                switchTo.frames(originalFC);
                scrollRootElement = getEyesDriver().findElement(By.tagName("html"));
            } else {
                scrollRootElement = this.scrollRootElement;
            }

            PositionProvider positionProvider = getElementPositionProvider(scrollRootElement);
            positionProvider.setPosition(elementLocation);
        }
    }

    private Region getViewportScrollBounds() {
        if (!getScrollToRegion()) {
            logger.log("WARNING: no region visibility strategy! returning an empty region!");
            return Region.EMPTY;
        }
        FrameChain originalFrameChain = getEyesDriver().getFrameChain().clone();
        EyesTargetLocator switchTo = (EyesTargetLocator) getEyesDriver().switchTo();
        switchTo.defaultContent();
        ScrollPositionProvider spp = new ScrollPositionProvider(logger, getJsExecutor());
        Location location;
        try {
            location = spp.getCurrentPosition();
        } catch (EyesDriverOperationException e) {
            logger.log("WARNING: " + e.getMessage());
            logger.log("Assuming position is 0,0");
            location = new Location(0, 0);
        }
        Region viewportBounds = new Region(location, getViewportSize());
        switchTo.frames(originalFrameChain);
        return viewportBounds;
    }

    protected MatchResult checkRegion(String name, ICheckSettings checkSettings) {
//        // If needed, scroll to the top/left of the element (additional help
//        // to make sure it's visible).
//        Point locationAsPoint = targetElement.getLocation();
//        RegionVisibilityStrategy regionVisibilityStrategy = regionVisibilityStrategyHandler.get();
//
//        regionVisibilityStrategy.moveToRegion(positionProvider,
//                new Location(locationAsPoint.getX(), locationAsPoint.getY()));

        MatchResult result = checkWindowBase(new RegionProvider() {
            @Override
            public Region getRegion() {
                Point p = targetElement.getLocation();
                Dimension d = targetElement.getSize();
                return new Region(p.getX(), p.getY(), d.getWidth(), d.getHeight(), CoordinatesType.CONTEXT_RELATIVE);
            }
        }, name, false, checkSettings);
        logger.verbose("Done! trying to scroll back to original position.");

        //regionVisibilityStrategy.returnToOriginalPosition(positionProvider);
        return result;
    }

    /**
     * See {@link #checkRegion(Region, int, String)}.
     * {@code tag} defaults to {@code null}.
     * Default match timeout is used.
     */
    public void checkRegion(Region region) {
        checkRegion(region, USE_DEFAULT_MATCH_TIMEOUT, null);
    }

    /**
     * Takes a snapshot of the application under test and matches a specific region within it with the expected output.
     * @param region       A non empty region representing the screen region to check.
     * @param matchTimeout The amount of time to retry matching. (Milliseconds)
     * @param tag          An optional tag to be associated with the snapshot.
     * @throws TestFailedException Thrown if a mismatch is detected and immediate failure reports are enabled.
     */
    public void checkRegion(final Region region, int matchTimeout, String tag) {

        if (getIsDisabled()) {
            logger.log(String.format("checkRegion([%s], %d, '%s'): Ignored", region, matchTimeout, tag));
            return;
        }

        ArgumentGuard.notNull(region, "region");

        logger.verbose(String.format("checkRegion([%s], %d, '%s')", region, matchTimeout, tag));

        super.checkWindowBase(
                new RegionProvider() {
                    public Region getRegion() {
                        return region;
                    }
                },
                tag,
                false,
                matchTimeout
        );
    }

    /**
     * See {@link #checkRegion(WebElement, String)}.
     * {@code tag} defaults to {@code null}.
     * @param element The element which represents the region to check.
     */
    public void checkRegion(WebElement element) {
        check(null, Target.region(element));
    }

    /**
     * If {@code stitchContent} is {@code false} then behaves the same as
     * {@link #checkRegion(WebElement)}, otherwise
     * behaves the same as {@link #checkElement(WebElement)}.
     * @param element       The element which represents the region to check.
     * @param stitchContent Whether to take a screenshot of the whole region and stitch if needed.
     */
    public void checkRegion(WebElement element, boolean stitchContent) {
        check(null, Target.region(element).fully(stitchContent));
    }

    /**
     * See {@link #checkRegion(WebElement, int, String)}.
     * Default match timeout is used.
     * @param element The element which represents the region to check.
     * @param tag     An optional tag to be associated with the snapshot.
     */
    public void checkRegion(WebElement element, String tag) {
        check(tag, Target.region(element));
    }

    /**
     * if {@code stitchContent} is {@code false} then behaves the same {@link
     * #checkRegion(WebElement, String)}. Otherwise
     * behaves the same as {@link #checkElement(WebElement, String)}.
     * @param element       The element which represents the region to check.
     * @param tag           An optional tag to be associated with the snapshot.
     * @param stitchContent Whether to take a screenshot of the whole region and stitch if needed.
     */
    public void checkRegion(WebElement element, String tag, boolean stitchContent) {
        check(tag, Target.region(element).fully(stitchContent));
    }

    /**
     * Takes a snapshot of the application under test and matches a region of
     * a specific element with the expected region output.
     * @param element      The element which represents the region to check.
     * @param matchTimeout The amount of time to retry matching. (Milliseconds)
     * @param tag          An optional tag to be associated with the snapshot.
     * @throws TestFailedException if a mismatch is detected and
     *                             immediate failure reports are enabled
     */
    public void checkRegion(final WebElement element, int matchTimeout, String tag) {
        ArgumentGuard.notNull(element, "element");
        check(tag, Target.region(element).timeout(matchTimeout));
    }

    /**
     * if {@code stitchContent} is {@code false} then behaves the same {@link
     * #checkRegion(WebElement, int, String)}. Otherwise
     * behaves the same as {@link #checkElement(WebElement, String)}.
     * @param element       The element which represents the region to check.
     * @param matchTimeout  The amount of time to retry matching. (Milliseconds)
     * @param tag           An optional tag to be associated with the snapshot.
     * @param stitchContent Whether to take a screenshot of the whole region and stitch if needed.
     */
    public void checkRegion(WebElement element, int matchTimeout, String tag, boolean stitchContent) {
        check(tag, Target.region(element).timeout(matchTimeout).fully(stitchContent));
    }

    /**
     * See {@link #checkRegion(By, String)}.
     * {@code tag} defaults to {@code null}.
     * @param selector The selector by which to specify which region to check.
     */
    public void checkRegion(By selector) {
        check(null, Target.region(selector));
    }

    /**
     * If {@code stitchContent} is {@code false} then behaves the same as
     * {@link #checkRegion(By)}. Otherwise, behaves the
     * same as {@code #checkElement(org.openqa.selenium.By)}
     * @param selector      The selector by which to specify which region to check.
     * @param stitchContent Whether to take a screenshot of the whole region and stitch if needed.
     */
    public void checkRegion(By selector, boolean stitchContent) {
        check(null, Target.region(selector).fully(stitchContent));
    }

    /**
     * See {@link #checkRegion(By, int, String)}.
     * Default match timeout is used.
     * @param selector The selector by which to specify which region to check.
     * @param tag      An optional tag to be associated with the screenshot.
     */
    public void checkRegion(By selector, String tag) {
        check(tag, Target.region(selector));
    }

    /**
     * If {@code stitchContent} is {@code false} then behaves the same as
     * {@link #checkRegion(By, String)}. Otherwise,
     * behaves the same as {@link #checkElement(By, String)}.
     * @param selector      The selector by which to specify which region to check.
     * @param tag           An optional tag to be associated with the screenshot.
     * @param stitchContent Whether to take a screenshot of the whole region and stitch if needed.
     */
    public void checkRegion(By selector, String tag, boolean stitchContent) {
        check(tag, Target.region(selector).fully(stitchContent));
    }

    /**
     * Takes a snapshot of the application under test and matches a region
     * specified by the given selector with the expected region output.
     * @param selector     The selector by which to specify which region to check.
     * @param matchTimeout The amount of time to retry matching. (Milliseconds)
     * @param tag          An optional tag to be associated with the screenshot.
     * @throws TestFailedException if a mismatch is detected and
     *                             immediate failure reports are enabled
     */
    public void checkRegion(By selector, int matchTimeout, String tag) {
        check(tag, Target.region(selector).timeout(matchTimeout));
    }

    /**
     * If {@code stitchContent} is {@code false} then behaves the same as
     * {@link #checkRegion(By, int, String)}. Otherwise,
     * behaves the same as {@link #checkElement(By, int, String)}.
     * @param selector      The selector by which to specify which region to check.
     * @param matchTimeout  The amount of time to retry matching. (Milliseconds)
     * @param tag           An optional tag to be associated with the screenshot.
     * @param stitchContent Whether to take a screenshot of the whole region and stitch if needed.
     */
    public void checkRegion(By selector, int matchTimeout, String tag, boolean stitchContent) {
        check(tag, Target.region(selector).timeout(matchTimeout).fully(stitchContent));
    }

    /**
     * See {@link #checkRegionInFrame(int, By, String)}.
     * {@code tag} defaults to {@code null}.
     * @param frameIndex The index of the frame to switch to. (The same index
     *                   as would be used in a call to
     *                   driver.switchTo().frame()).
     * @param selector   The selector by which to specify which region to check inside the frame.
     */
    public void checkRegionInFrame(int frameIndex, By selector) {
        checkRegionInFrame(frameIndex, selector, false);
    }

    /**
     * See {@link #checkRegionInFrame(int, By, String)}.
     * {@code tag} defaults to {@code null}.
     * @param frameIndex    The index of the frame to switch to. (The same index
     *                      as would be used in a call to
     *                      driver.switchTo().frame()).
     * @param selector      The selector by which to specify which region to check inside the frame.
     * @param stitchContent If {@code true}, stitch the internal content of
     *                      the region (i.e., perform
     *                      {@link #checkElement(By, int, String)} on the
     *                      region.
     */
    public void checkRegionInFrame(int frameIndex, By selector, boolean stitchContent) {
        checkRegionInFrame(frameIndex, selector, null, stitchContent);
    }

    /**
     * See {@link #checkRegionInFrame(int, By, String, boolean)}.
     * {@code stitchContent} defaults to {@code false}.
     * @param frameIndex The index of the frame to switch to. (The same index
     *                   as would be used in a call to
     *                   driver.switchTo().frame()).
     * @param selector   The selector by which to specify which region to check inside the frame.
     * @param tag        An optional tag to be associated with the screenshot.
     */
    public void checkRegionInFrame(int frameIndex, By selector, String tag) {
        checkRegionInFrame(frameIndex, selector, tag, false);
    }

    /**
     * See {@link #checkRegionInFrame(int, By, int, String, boolean)}.
     * Default match timeout is used.
     * @param frameIndex    The index of the frame to switch to. (The same index
     *                      as would be used in a call to
     *                      driver.switchTo().frame()).
     * @param selector      The selector by which to specify which region to check inside the frame.
     * @param tag           An optional tag to be associated with the screenshot.
     * @param stitchContent If {@code true}, stitch the internal content of
     *                      the region (i.e., perform
     *                      {@link #checkElement(By, int, String)} on the
     *                      region.
     */
    public void checkRegionInFrame(int frameIndex, By selector, String tag, boolean stitchContent) {
        checkRegionInFrame(frameIndex, selector, USE_DEFAULT_MATCH_TIMEOUT,
                tag, stitchContent);
    }

    /**
     * See {@link #checkRegionInFrame(int, By, int, String, boolean)}.
     * {@code stitchContent} defaults to {@code false}.
     * @param frameIndex   The index of the frame to switch to. (The same index
     *                     as would be used in a call to
     *                     driver.switchTo().frame()).
     * @param selector     The selector by which to specify which region to check inside the frame.
     * @param matchTimeout The amount of time to retry matching. (Milliseconds)
     * @param tag          An optional tag to be associated with the screenshot.
     */
    public void checkRegionInFrame(int frameIndex, By selector, int matchTimeout, String tag) {
        checkRegionInFrame(frameIndex, selector, matchTimeout, tag, false);
    }

    /**
     * Switches into the given frame, takes a snapshot of the application under
     * test and matches a region specified by the given selector.
     * @param frameIndex    The index of the frame to switch to. (The same index
     *                      as would be used in a call to
     *                      driver.switchTo().frame()).
     * @param selector      A Selector specifying the region to check.
     * @param matchTimeout  The amount of time to retry matching. (Milliseconds)
     * @param tag           An optional tag to be associated with the snapshot.
     * @param stitchContent If {@code true}, stitch the internal content of
     *                      the region (i.e., perform
     *                      {@link #checkElement(By, int, String)} on the
     *                      region.
     */
    public void checkRegionInFrame(int frameIndex, By selector,
                                   int matchTimeout, String tag,
                                   boolean stitchContent) {
        check(tag, Target.frame(frameIndex).region(selector).timeout(matchTimeout).fully(stitchContent));
    }

    /**
     * See {@link #checkRegionInFrame(String, By, int, String, boolean)}.
     * {@code stitchContent} defaults to {@code null}.
     * @param frameNameOrId The name or id of the frame to switch to. (as would
     *                      be used in a call to driver.switchTo().frame()).
     * @param selector      A Selector specifying the region to check.
     */
    public void checkRegionInFrame(String frameNameOrId, By selector) {
        checkRegionInFrame(frameNameOrId, selector, false);
    }

    /**
     * See {@link #checkRegionInFrame(String, By, int, String, boolean)}.
     * {@code tag} defaults to {@code null}.
     * @param frameNameOrId The name or id of the frame to switch to. (as would
     *                      be used in a call to driver.switchTo().frame()).
     * @param selector      A Selector specifying the region to check.
     * @param stitchContent If {@code true}, stitch the internal content of
     *                      the region (i.e., perform
     *                      {@link #checkElement(By, int, String)} on the
     *                      region.
     */
    public void checkRegionInFrame(String frameNameOrId, By selector, boolean stitchContent) {
        checkRegionInFrame(frameNameOrId, selector, null, stitchContent);
    }

    /**
     * See {@link #checkRegionInFrame(String, By, int, String, boolean)}.
     * {@code stitchContent} defaults to {@code null}.
     * @param frameNameOrId The name or id of the frame to switch to. (as would
     *                      be used in a call to driver.switchTo().frame()).
     * @param selector      A Selector specifying the region to check.
     * @param tag           An optional tag to be associated with the snapshot.
     */
    public void checkRegionInFrame(String frameNameOrId, By selector,
                                   String tag) {
        checkRegionInFrame(frameNameOrId, selector, USE_DEFAULT_MATCH_TIMEOUT,
                tag, false);
    }

    /**
     * See {@link #checkRegionInFrame(String, By, int, String, boolean)}.
     * Default match timeout is used
     * @param frameNameOrId The name or id of the frame to switch to. (as would
     *                      be used in a call to driver.switchTo().frame()).
     * @param selector      A Selector specifying the region to check.
     * @param tag           An optional tag to be associated with the snapshot.
     * @param stitchContent If {@code true}, stitch the internal content of
     *                      the region (i.e., perform
     *                      {@link #checkElement(By, int, String)} on the
     *                      region.
     */
    public void checkRegionInFrame(String frameNameOrId, By selector,
                                   String tag, boolean stitchContent) {
        checkRegionInFrame(frameNameOrId, selector, USE_DEFAULT_MATCH_TIMEOUT,
                tag, stitchContent);
    }

    /**
     * See {@link #checkRegionInFrame(String, By, int, String, boolean)}.
     * {@code stitchContent} defaults to {@code false}.
     * @param frameNameOrId The name or id of the frame to switch to. (as would
     *                      be used in a call to driver.switchTo().frame()).
     * @param selector      A Selector specifying the region to check inside the frame.
     * @param matchTimeout  The amount of time to retry matching. (Milliseconds)
     * @param tag           An optional tag to be associated with the snapshot.
     */
    public void checkRegionInFrame(String frameNameOrId, By selector,
                                   int matchTimeout, String tag) {
        checkRegionInFrame(frameNameOrId, selector, matchTimeout, tag, false);
    }

    /**
     * Switches into the given frame, takes a snapshot of the application under
     * test and matches a region specified by the given selector.
     * @param frameNameOrId The name or id of the frame to switch to. (as would
     *                      be used in a call to driver.switchTo().frame()).
     * @param selector      A Selector specifying the region to check inside the frame.
     * @param matchTimeout  The amount of time to retry matching. (Milliseconds)
     * @param tag           An optional tag to be associated with the snapshot.
     * @param stitchContent If {@code true}, stitch the internal content of
     *                      the region (i.e., perform
     *                      {@link #checkElement(By, int, String)} on the region.
     */
    public void checkRegionInFrame(String frameNameOrId, By selector,
                                   int matchTimeout, String tag,
                                   boolean stitchContent) {
        check(tag, Target.frame(frameNameOrId).region(selector).timeout(matchTimeout).fully(stitchContent));
    }

    /**
     * See {@link #checkRegionInFrame(WebElement, By, boolean)}.
     * {@code stitchContent} defaults to {@code null}.
     * @param frameReference The element which is the frame to switch to. (as
     *                       would be used in a call to
     *                       driver.switchTo().frame()).
     * @param selector       A Selector specifying the region to check inside the frame.
     */
    public void checkRegionInFrame(WebElement frameReference, By selector) {
        checkRegionInFrame(frameReference, selector, false);
    }

    /**
     * See {@link #checkRegionInFrame(WebElement, By, String, boolean)}.
     * {@code tag} defaults to {@code null}.
     * @param frameReference The element which is the frame to switch to. (as
     *                       would be used in a call to
     *                       driver.switchTo().frame()).
     * @param selector       A Selector specifying the region to check inside the frame.
     * @param stitchContent  If {@code true}, stitch the internal content of
     *                       the region (i.e., perform
     *                       {@link #checkElement(By, int, String)} on the
     *                       region.
     */
    public void checkRegionInFrame(WebElement frameReference, By selector, boolean stitchContent) {
        checkRegionInFrame(frameReference, selector, null, stitchContent);
    }

    /**
     * See {@link #checkRegionInFrame(WebElement, By, String, boolean)}.
     * {@code stitchContent} defaults to {@code false}.
     * @param frameReference The element which is the frame to switch to. (as
     *                       would be used in a call to
     *                       driver.switchTo().frame()).
     * @param selector       A Selector specifying the region to check inside the frame.
     * @param tag            An optional tag to be associated with the snapshot.
     */
    public void checkRegionInFrame(WebElement frameReference, By selector, String tag) {
        checkRegionInFrame(frameReference, selector, tag, false);
    }

    /**
     * See {@link #checkRegionInFrame(WebElement, By, int, String, boolean)}.
     * Default match timeout is used.
     * @param frameReference The element which is the frame to switch to. (as
     *                       would be used in a call to
     *                       driver.switchTo().frame()).
     * @param selector       A Selector specifying the region to check inside the frame.
     * @param tag            An optional tag to be associated with the snapshot.
     * @param stitchContent  If {@code true}, stitch the internal content of
     *                       the region (i.e., perform
     *                       {@link #checkElement(By, int, String)} on the
     *                       region.
     */
    public void checkRegionInFrame(WebElement frameReference, By selector,
                                   String tag, boolean stitchContent) {
        checkRegionInFrame(frameReference, selector, USE_DEFAULT_MATCH_TIMEOUT,
                tag, stitchContent);
    }

    /**
     * See {@link #checkRegionInFrame(WebElement, By, int, String, boolean)}.
     * {@code stitchContent} defaults to {@code false}.
     * @param frameReference The element which is the frame to switch to. (as
     *                       would be used in a call to
     *                       driver.switchTo().frame()).
     * @param selector       A Selector specifying the region to check inside the frame.
     * @param matchTimeout   The amount of time to retry matching. (Milliseconds)
     * @param tag            An optional tag to be associated with the snapshot.
     */
    public void checkRegionInFrame(WebElement frameReference, By selector,
                                   int matchTimeout, String tag) {
        checkRegionInFrame(frameReference, selector, matchTimeout, tag, false);
    }

    /**
     * Switches into the given frame, takes a snapshot of the application under
     * test and matches a region specified by the given selector.
     * @param frameReference The element which is the frame to switch to. (as
     *                       would be used in a call to
     *                       driver.switchTo().frame()).
     * @param selector       A Selector specifying the region to check.
     * @param matchTimeout   The amount of time to retry matching. (Milliseconds)
     * @param tag            An optional tag to be associated with the snapshot.
     * @param stitchContent  If {@code true}, stitch the internal content of
     *                       the region (i.e., perform
     *                       {@link #checkElement(By, int, String)} on the
     *                       region.
     */
    public void checkRegionInFrame(WebElement frameReference, By selector,
                                   int matchTimeout, String tag,
                                   boolean stitchContent) {
        check(tag, Target.frame(frameReference).region(selector).timeout(matchTimeout).fully(stitchContent));
    }

    protected double extractDevicePixelRatio() {
        return EyesSeleniumUtils.getDevicePixelRatio(getJsExecutor());
    }

    protected void tryUpdateDevicePixelRatio() {
        logger.verbose("Trying to update device pixel ratio...");
        try {
            devicePixelRatio = extractDevicePixelRatio();
        } catch (Exception e) {
            logger.verbose(
                    "Failed to extract device pixel ratio! Using default.");
            devicePixelRatio = DEFAULT_DEVICE_PIXEL_RATIO;
        }
        logger.verbose(String.format("Device pixel ratio: %f", devicePixelRatio));
    }

    /**
     * Updates the state of scaling related parameters.
     */
    protected ScaleProviderFactory updateScalingParams() {
        // Update the scaling params only if we haven't done so yet, and the user hasn't set anything else manually.
        if (scaleProviderHandler.get() instanceof NullScaleProvider) {
            ScaleProviderFactory factory;


            logger.verbose("Setting scale provider...");
            try {
                factory = getScaleProviderFactory();
            } catch (Exception e) {
                // This can happen in Appium for example.
                logger.verbose("Failed to set ContextBasedScaleProvider.");
                logger.verbose("Using FixedScaleProvider instead...");
                factory = new FixedScaleProviderFactory(1 / getDevicePixelRatio(), scaleProviderHandler);
            }
            logger.verbose("Done!");
            return factory;
        }
        // If we already have a scale provider set, we'll just use it, and pass a mock as provider handler.
        PropertyHandler<ScaleProvider> nullProvider = new SimplePropertyHandler<>();
        return new ScaleProviderIdentityFactory(scaleProviderHandler.get(), nullProvider);
    }

    protected ScaleProviderFactory getScaleProviderFactory() {
        WebElement element = getEyesDriver().findElement(By.tagName("html"));
        RectangleSize entireSize = EyesSeleniumUtils.getEntireElementSize(getJsExecutor(), element);
        return new ContextBasedScaleProviderFactory(logger, entireSize,
                viewportSizeHandler.get(), getDevicePixelRatio(), false,
                scaleProviderHandler);
    }

    /**
     * Verifies the current frame.
     * @param matchTimeout The amount of time to retry matching. (Milliseconds)
     * @param tag          An optional tag to be associated with the snapshot.
     */
    protected void checkCurrentFrame(int matchTimeout, String tag) {
        try {
            logger.verbose(String.format("CheckCurrentFrame(%d, '%s')", matchTimeout, tag));

            checkFrameOrElement = true;

            logger.verbose("Getting screenshot as base64..");
            String screenshot64 = getEyesDriver().getScreenshotAs(OutputType.BASE64);
            logger.verbose("Done! Creating image object...");
            BufferedImage screenshotImage = ImageUtils.imageFromBase64(screenshot64);

            // FIXME - Scaling should be handled in a single place instead
            ScaleProvider scaleProvider = updateScalingParams().getScaleProvider(screenshotImage.getWidth());

            screenshotImage = ImageUtils.scaleImage(screenshotImage, scaleProvider);
            logger.verbose("Done! Building required object...");
            final EyesWebDriverScreenshot screenshot = new EyesWebDriverScreenshot(logger, getEyesDriver(), screenshotImage);
            logger.verbose("Done!");

            logger.verbose("replacing regionToCheck");
            setRegionToCheck(screenshot.getFrameWindow());

            super.checkWindowBase(NullRegionProvider.INSTANCE, tag, false, matchTimeout);
        } finally {
            checkFrameOrElement = false;
            regionToCheck = null;
        }
    }

    /**
     * See {@link #checkFrame(String, int, String)}.
     * {@code tag} defaults to {@code null}. Default match timeout is used.
     */
    public void checkFrame(String frameNameOrId) {
        check(null, Target.frame(frameNameOrId));
    }

    /**
     * See {@link #checkFrame(String, int, String)}.
     * Default match timeout is used.
     */
    public void checkFrame(String frameNameOrId, String tag) {
        check(tag, Target.frame(frameNameOrId).fully());
    }

    /**
     * Matches the frame given as parameter, by switching into the frame and
     * using stitching to get an image of the frame.
     * @param frameNameOrId The name or id of the frame to check. (The same
     *                      name/id as would be used in a call to
     *                      driver.switchTo().frame()).
     * @param matchTimeout  The amount of time to retry matching. (Milliseconds)
     * @param tag           An optional tag to be associated with the match.
     */
    public void checkFrame(String frameNameOrId, int matchTimeout, String tag) {
        check(tag, Target.frame(frameNameOrId).timeout(matchTimeout).fully());
    }

    /**
     * See {@link #checkFrame(int, int, String)}.
     * {@code tag} defaults to {@code null}. Default match timeout is used.
     */
    public void checkFrame(int frameIndex) {
        checkFrame(frameIndex, USE_DEFAULT_MATCH_TIMEOUT, null);
    }

    /**
     * See {@link #checkFrame(int, int, String)}.
     * Default match timeout is used.
     */
    public void checkFrame(int frameIndex, String tag) {
        checkFrame(frameIndex, USE_DEFAULT_MATCH_TIMEOUT, tag);
    }

    /**
     * Matches the frame given as parameter, by switching into the frame and
     * using stitching to get an image of the frame.
     * @param frameIndex   The index of the frame to switch to. (The same index
     *                     as would be used in a call to
     *                     driver.switchTo().frame()).
     * @param matchTimeout The amount of time to retry matching. (Milliseconds)
     * @param tag          An optional tag to be associated with the match.
     */
    public void checkFrame(int frameIndex, int matchTimeout, String tag) {
        if (getIsDisabled()) {
            logger.log(String.format("CheckFrame(%d, %d, '%s'): Ignored", frameIndex, matchTimeout, tag));
            return;
        }

        ArgumentGuard.greaterThanOrEqualToZero(frameIndex, "frameIndex");

        logger.log(String.format("CheckFrame(%d, %d, '%s')", frameIndex, matchTimeout, tag));

        check(tag, Target.frame(frameIndex).timeout(matchTimeout).fully());
    }

    /**
     * See {@link #checkFrame(WebElement, int, String)}.
     * {@code tag} defaults to {@code null}.
     * Default match timeout is used.
     */
    public void checkFrame(WebElement frameReference) {
        checkFrame(frameReference, USE_DEFAULT_MATCH_TIMEOUT, null);
    }

    /**
     * See {@link #checkFrame(WebElement, int, String)}.
     * Default match timeout is used.
     */
    public void checkFrame(WebElement frameReference, String tag) {
        checkFrame(frameReference, USE_DEFAULT_MATCH_TIMEOUT, tag);
    }

    /**
     * Matches the frame given as parameter, by switching into the frame and
     * using stitching to get an image of the frame.
     * @param frameReference The element which is the frame to switch to. (as
     *                       would be used in a call to
     *                       driver.switchTo().frame() ).
     * @param matchTimeout   The amount of time to retry matching (milliseconds).
     * @param tag            An optional tag to be associated with the match.
     */
    public void checkFrame(WebElement frameReference, int matchTimeout, String tag) {
        check(tag, Target.frame(frameReference).timeout(matchTimeout));
    }

    /**
     * Matches the frame given by the frames path, by switching into the frame
     * and using stitching to get an image of the frame.
     * @param framePath    The path to the frame to check. This is a list of
     *                     frame names/IDs (where each frame is nested in the
     *                     previous frame).
     * @param matchTimeout The amount of time to retry matching (milliseconds).
     * @param tag          An optional tag to be associated with the match.
     */
    public void checkFrame(String[] framePath, int matchTimeout, String tag) {

        SeleniumCheckSettings settings = Target.frame(framePath[0]);
        for (int i = 1; i < framePath.length; i++) {
            settings.frame(framePath[i]);
        }
        check(tag, settings.timeout(matchTimeout).fully());
    }

    /**
     * See {@link #checkFrame(String[], int, String)}.
     * Default match timeout is used.
     */
    public void checkFrame(String[] framesPath, String tag) {
        checkFrame(framesPath, USE_DEFAULT_MATCH_TIMEOUT, tag);
    }

    /**
     * See {@link #checkFrame(String[], int, String)}.
     * Default match timeout is used.
     * {@code tag} defaults to {@code null}.
     */
    public void checkFrame(String[] framesPath) {
        checkFrame(framesPath, USE_DEFAULT_MATCH_TIMEOUT, null);
    }

    /**
     * Switches into the given frame, takes a snapshot of the application under
     * test and matches a region specified by the given selector.
     * @param framePath     The path to the frame to check. This is a list of
     *                      frame names/IDs (where each frame is nested in the previous frame).
     * @param selector      A Selector specifying the region to check.
     * @param matchTimeout  The amount of time to retry matching (milliseconds).
     * @param tag           An optional tag to be associated with the snapshot.
     * @param stitchContent Whether or not to stitch the internal content of the
     *                      region (i.e., perform {@link #checkElement(By, int, String)} on the region.
     */
    public void checkRegionInFrame(String[] framePath, By selector,
                                   int matchTimeout, String tag,
                                   boolean stitchContent) {

        SeleniumCheckSettings settings = Target.frame(framePath[0]);
        for (int i = 1; i < framePath.length; i++) {
            settings = settings.frame(framePath[i]);
        }
        check(tag, settings.region(selector).timeout(matchTimeout).fully(stitchContent));
    }

    /**
     * See {@link #checkRegionInFrame(String[], By, int, String, boolean)}.
     * {@code stitchContent} defaults to {@code false}.
     */
    public void checkRegionInFrame(String[] framePath, By selector, int matchTimeout, String tag) {
        checkRegionInFrame(framePath, selector, matchTimeout, tag, false);
    }

    /**
     * See {@link #checkRegionInFrame(String[], By, int, String)}.
     * Default match timeout is used.
     */
    public void checkRegionInFrame(String[] framePath, By selector, String tag) {
        checkRegionInFrame(framePath, selector, USE_DEFAULT_MATCH_TIMEOUT, tag);
    }

    /**
     * See {@link #checkRegionInFrame(String[], By, int, String)}.
     * Default match timeout is used.
     * {@code tag} defaults to {@code null}.
     */
    public void checkRegionInFrame(String[] framePath, By selector) {
        checkRegionInFrame(framePath, selector, USE_DEFAULT_MATCH_TIMEOUT, null);
    }

    /**
     * See {@link #checkElement(WebElement, String)}.
     * {@code tag} defaults to {@code null}.
     */
    public void checkElement(WebElement element) {
        check(null, Target.region(element).fully());
    }

    /**
     * See {@link #checkElement(WebElement, int, String)}.
     * Default match timeout is used.
     */
    public void checkElement(WebElement element, String tag) {
        check(tag, Target.region(element).fully());
    }

    private MatchResult checkElement(String name, ICheckSettings checkSettings) {
        return this.checkElement(this.targetElement, name, checkSettings);
    }

    protected MatchResult checkElement(WebElement element, String name, ICheckSettings checkSettings) {

        // Since the element might already have been found using EyesWebDriver.
        final EyesRemoteWebElement eyesElement = (element instanceof EyesRemoteWebElement) ?
                (EyesRemoteWebElement) element : new EyesRemoteWebElement(logger, getEyesDriver(), element);

        this.regionToCheck = null;
        PositionMemento originalPositionMemento = positionProviderHandler.get().getState();

        ensureElementVisible(targetElement);

        PositionProvider scrollPositionProvider = new ScrollPositionProvider(logger, getJsExecutor());
        Location originalScrollPosition = scrollPositionProvider.getCurrentPosition();

        String originalOverflow = null;

        Point pl = eyesElement.getLocation();
        MatchResult result;
        try {
            checkFrameOrElement = true;

            String displayStyle = eyesElement.getComputedStyle("display");

            if (getConfig().getHideScrollbars()) {
                originalOverflow = eyesElement.getOverflow();
                eyesElement.setOverflow("hidden");
            }

            int elementWidth = eyesElement.getClientWidth();
            int elementHeight = eyesElement.getClientHeight();

            if (!displayStyle.equals("inline") &&
                    elementHeight <= effectiveViewport.getHeight() &&
                    elementWidth <= effectiveViewport.getWidth()) {
                elementPositionProvider = new ElementPositionProvider(logger, getEyesDriver(), eyesElement);
            } else {
                elementPositionProvider = null;
            }

            int borderLeftWidth = eyesElement.getComputedStyleInteger("border-left-width");
            int borderTopWidth = eyesElement.getComputedStyleInteger("border-top-width");

            final Region elementRegion = new Region(
                    pl.getX() + borderLeftWidth, pl.getY() + borderTopWidth,
                    elementWidth, elementHeight, CoordinatesType.SCREENSHOT_AS_IS);

            logger.verbose("Element region: " + elementRegion);

            logger.verbose("replacing regionToCheck");
            regionToCheck = elementRegion;

            if (!effectiveViewport.isSizeEmpty()) {
                regionToCheck.intersect(effectiveViewport);
            }

            result = checkWindowBase(NullRegionProvider.INSTANCE, name, false, checkSettings);
        } finally {
            if (originalOverflow != null) {
                eyesElement.setOverflow(originalOverflow);
            }

            checkFrameOrElement = false;

            positionProviderHandler.get().restoreState(originalPositionMemento);
            scrollPositionProvider.setPosition(originalScrollPosition);
            regionToCheck = null;
            elementPositionProvider = null;
        }

        return result;
    }

    /**
     * Takes a snapshot of the application under test and matches a specific
     * element with the expected region output.
     * @param element      The element to check.
     * @param matchTimeout The amount of time to retry matching. (Milliseconds)
     * @param tag          An optional tag to be associated with the snapshot.
     * @throws TestFailedException if a mismatch is detected and immediate failure reports are enabled
     */
    public void checkElement(WebElement element, int matchTimeout, String tag) {
        check(tag, Target.region(element).timeout(matchTimeout).fully());
    }

    /**
     * See {@link #checkElement(By, String)}.
     * {@code tag} defaults to {@code null}.
     */
    public void checkElement(By selector) {
        check(null, Target.region(selector).fully());
    }

    /**
     * See {@link #checkElement(By, int, String)}.
     * Default match timeout is used.
     */
    public void checkElement(By selector, String tag) {
        check(tag, Target.region(selector).fully());
    }

    /**
     * Takes a snapshot of the application under test and matches an element
     * specified by the given selector with the expected region output.
     * @param selector     Selects the element to check.
     * @param matchTimeout The amount of time to retry matching. (Milliseconds)
     * @param tag          An optional tag to be associated with the screenshot.
     * @throws TestFailedException if a mismatch is detected and
     *                             immediate failure reports are enabled
     */
    public void checkElement(By selector, int matchTimeout, String tag) {
        check(tag, Target.region(selector).timeout(matchTimeout).fully());
    }

    /**
     * Adds a mouse trigger.
     * @param action  Mouse action.
     * @param control The control on which the trigger is activated (context relative coordinates).
     * @param cursor  The cursor's position relative to the control.
     */
    public void addMouseTrigger(MouseAction action, Region control, Location cursor) {
        if (getIsDisabled()) {
            logger.verbose(String.format("Ignoring %s (disabled)", action));
            return;
        }

        // Triggers are actually performed on the previous window.
        if (lastScreenshot == null) {
            logger.verbose(String.format("Ignoring %s (no screenshot)", action));
            return;
        }

        if (!FrameChain.isSameFrameChain(getEyesDriver().getFrameChain(),
                ((EyesWebDriverScreenshot) lastScreenshot).getFrameChain())) {
            logger.verbose(String.format("Ignoring %s (different frame)", action));
            return;
        }

        addMouseTriggerBase(action, control, cursor);
    }

    /**
     * Adds a mouse trigger.
     * @param action  Mouse action.
     * @param element The WebElement on which the click was called.
     */
    public void addMouseTrigger(MouseAction action, WebElement element) {
        if (getIsDisabled()) {
            logger.verbose(String.format("Ignoring %s (disabled)", action));
            return;
        }

        ArgumentGuard.notNull(element, "element");

        Point pl = element.getLocation();
        Dimension ds = element.getSize();

        Region elementRegion = new Region(pl.getX(), pl.getY(), ds.getWidth(),
                ds.getHeight());

        // Triggers are actually performed on the previous window.
        if (lastScreenshot == null) {
            logger.verbose(String.format("Ignoring %s (no screenshot)", action));
            return;
        }

        if (!FrameChain.isSameFrameChain(getEyesDriver().getFrameChain(),
                ((EyesWebDriverScreenshot) lastScreenshot).getFrameChain())) {
            logger.verbose(String.format("Ignoring %s (different frame)", action));
            return;
        }

        // Get the element region which is intersected with the screenshot,
        // so we can calculate the correct cursor position.
        elementRegion = lastScreenshot.getIntersectedRegion
                (elementRegion, CoordinatesType.CONTEXT_RELATIVE);

        addMouseTriggerBase(action, elementRegion,
                elementRegion.getMiddleOffset());
    }

    /**
     * Adds a keyboard trigger.
     * @param control The control's context-relative region.
     * @param text    The trigger's text.
     */
    public void addTextTrigger(Region control, String text) {
        if (getIsDisabled()) {
            logger.verbose(String.format("Ignoring '%s' (disabled)", text));
            return;
        }

        if (lastScreenshot == null) {
            logger.verbose(String.format("Ignoring '%s' (no screenshot)", text));
            return;
        }

        if (!FrameChain.isSameFrameChain(getEyesDriver().getFrameChain(),
                ((EyesWebDriverScreenshot) lastScreenshot).getFrameChain())) {
            logger.verbose(String.format("Ignoring '%s' (different frame)", text));
            return;
        }

        addTextTriggerBase(control, text);
    }

    /**
     * Adds a keyboard trigger.
     * @param element The element for which we sent keys.
     * @param text    The trigger's text.
     */
    public void addTextTrigger(WebElement element, String text) {
        if (getIsDisabled()) {
            logger.verbose(String.format("Ignoring '%s' (disabled)", text));
            return;
        }

        ArgumentGuard.notNull(element, "element");

        Point pl = element.getLocation();
        Dimension ds = element.getSize();

        Region elementRegion = new Region(pl.getX(), pl.getY(), ds.getWidth(), ds.getHeight());

        addTextTrigger(elementRegion, text);
    }

    /**
     * Use this method only if you made a previous call to {@link #open
     * (WebDriver, String, String)} or one of its variants.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public RectangleSize getViewportSize() {
        RectangleSize viewportSize = viewportSizeHandler.get();
        if (viewportSize == null) {
            viewportSize = getEyesDriver().getDefaultContentViewportSize();
        }
        return viewportSize;
    }

    /**
     * Call this method if for some
     * reason you don't want to call {@link #open(WebDriver, String, String)}
     * (or one of its variants) yet.
     * @param driver The driver to use for getting the viewport.
     * @return The viewport size of the current context.
     */
    public static RectangleSize getViewportSize(WebDriver driver) {
        ArgumentGuard.notNull(driver, "driver");
        return EyesSeleniumUtils.getViewportSizeOrDisplaySize(new Logger(), driver);
    }

    /**
     * Use this method only if you made a previous call to {@link #open
     * (WebDriver, String, String)} or one of its variants.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected void setViewportSize(RectangleSize size) {
        if (viewportSizeHandler instanceof ReadOnlyPropertyHandler) {
            logger.verbose("Ignored (viewport size given explicitly)");
            return;
        }

        if (!isMobileDevice(getEyesDriver())) {
            FrameChain originalFrame = getEyesDriver().getFrameChain();
            getEyesDriver().switchTo().defaultContent();

            try {
                EyesSeleniumUtils.setViewportSize(logger, getEyesDriver(), size);
                effectiveViewport = new Region(Location.ZERO, size);
            } catch (EyesException e1) {
                // Just in case the user catches this error
                ((EyesTargetLocator) getEyesDriver().switchTo()).frames(originalFrame);

                throw new TestFailedException("Failed to set the viewport size", e1);
            }
            ((EyesTargetLocator) getEyesDriver().switchTo()).frames(originalFrame);
        }

        viewportSizeHandler.set(new RectangleSize(size.getWidth(), size.getHeight()));
    }

    /**
     * Set the viewport size using the driver. Call this method if for some
     * reason you don't want to call {@link #open(WebDriver, String, String)}
     * (or one of its variants) yet.
     * @param driver The driver to use for setting the viewport.
     * @param size   The required viewport size.
     */
    public static void setViewportSize(WebDriver driver, RectangleSize size) {
        ArgumentGuard.notNull(driver, "driver");
        EyesSeleniumUtils.setViewportSize(new Logger(), driver, size);
    }

    @Override
    protected void beforeOpen() {
    }

    private void trySwitchToFrames(WebDriver driver, EyesTargetLocator switchTo, FrameChain frames) {
        if (isMobileDevice(driver)) {
            return;
        }
        try {
            switchTo.frames(frames);
        } catch (WebDriverException e) {
            logger.log("WARNING: Failed to switch to original frame chain! " + e.getMessage());
        }
    }

    private FrameChain tryHideScrollbars() {
        if (isMobileDevice(getEyesDriver())) {
            return new FrameChain(logger);
        }
        if (getConfig().getHideScrollbars() || (getConfig().getStitchMode() == StitchMode.CSS && stitchContent)) {
            FrameChain originalFC = getEyesDriver().getFrameChain().clone();
            FrameChain fc = getEyesDriver().getFrameChain().clone();
            Frame frame = fc.peek();
            if (fc.size() > 0) {
                while (fc.size() > 0) {
                    logger.verbose("fc.Count = " + fc.size());
                    if (stitchContent || fc.size() != originalFC.size()) {
                        if (frame != null) {
                            frame.hideScrollbars(getEyesDriver());
                        } else {
                            logger.verbose("hiding scrollbars of element (1): " + this.scrollRootElement);
                            EyesSeleniumUtils.setOverflow(getEyesDriver(), "hidden", scrollRootElement);
                        }
                    }
                    getEyesDriver().switchTo().parentFrame();
                    fc.pop();
                    frame = fc.peek();
                }
            } else {
                logger.verbose("hiding scrollbars of element (2): " + scrollRootElement);
                this.originalOverflow = EyesSeleniumUtils.setOverflow(getEyesDriver(), "hidden", scrollRootElement);
            }
            logger.verbose("switching back to original frame");
            ((EyesTargetLocator) getEyesDriver().switchTo()).frames(originalFC);
            logger.verbose("done hiding scrollbars.");
            return originalFC;
        }
        return new FrameChain(logger);
    }

    private void tryRestoreScrollbars(FrameChain frameChain) {
        if (isMobileDevice(getEyesDriver())) {
            return;
        }
        if (getConfig().getHideScrollbars() || (getConfig().getStitchMode() == StitchMode.CSS && stitchContent)) {
            ((EyesTargetLocator) getEyesDriver().switchTo()).frames(frameChain);
            FrameChain originalFC = frameChain.clone();
            FrameChain fc = frameChain.clone();
            if (fc.size() > 0) {
                while (fc.size() > 0) {
                    Frame frame = fc.pop();
                    frame.returnToOriginalOverflow(getEyesDriver());
                    EyesTargetLocator.parentFrame(getEyesDriver().getRemoteWebDriver().switchTo(), fc);
                }
            } else {
                logger.verbose("returning overflow of element to its original value: " + scrollRootElement);
                EyesSeleniumUtils.setOverflow(getEyesDriver(), originalOverflow, scrollRootElement);
            }
            ((EyesTargetLocator) getEyesDriver().switchTo()).frames(originalFC);
            logger.verbose("done restoring scrollbars.");
        } else {
            logger.verbose("no need to restore scrollbars.");
        }
        getEyesDriver().getFrameChain().clear();
    }

    @Override
    protected EyesScreenshot getSubScreenshot(EyesScreenshot screenshot, Region region, ICheckSettingsInternal
            checkSettingsInternal) {
        ISeleniumCheckTarget seleniumCheckTarget = (checkSettingsInternal instanceof ISeleniumCheckTarget) ? (ISeleniumCheckTarget) checkSettingsInternal : null;

        logger.verbose("original region: " + region);
        region = regionPositionCompensation.compensateRegionPosition(region, getDevicePixelRatio());
        logger.verbose("compensated region: " + region);

        if (seleniumCheckTarget == null) {
            // we should't get here, but just in case
            return screenshot.getSubScreenshot(region, false);
        }

        // For check frame continue as usual
        if (seleniumCheckTarget.getFrameChain().size() > 0) {
            return screenshot.getSubScreenshot(region, false);
        }

        // For check region, we want the screenshot window to know it's a region.
        return ((EyesWebDriverScreenshot) screenshot).getSubScreenshotForRegion(region, false);
    }


    protected EyesScreenshot getFrameOrElementScreenshot() {
        logger.verbose("Check frame/element requested");

        FrameChain originalFrameChain = getEyesDriver().getFrameChain().clone();
        EyesTargetLocator switchTo = (EyesTargetLocator) getEyesDriver().switchTo();

        ScaleProviderFactory scaleProviderFactory = updateScalingParams();
        FullPageCaptureAlgorithm algo = createFullPageCaptureAlgorithm(scaleProviderFactory);

        EyesWebDriverScreenshot result;

        Object activeElement = null;
        if (getHideCaret()) {
            try {
                activeElement = getEyesDriver().executeScript("var activeElement = document.activeElement; activeElement && activeElement.blur(); return activeElement;");
            } catch (WebDriverException e) {
                logger.verbose("WARNING: Cannot hide caret! " + e.getMessage());
            }
        }

        switchTo.frames(originalFrameChain);

        BufferedImage entireFrameOrElement;
        if (elementPositionProvider == null) {
            WebElement scrollRootElement = getEyesDriver().findElement(By.tagName("html"));
            PositionProvider positionProvider = this.getElementPositionProvider(scrollRootElement);
            entireFrameOrElement = algo.getStitchedRegion(regionToCheck, null, positionProvider);
        } else {
            entireFrameOrElement = algo.getStitchedRegion(regionToCheck, null, elementPositionProvider);
        }

        logger.verbose("Building screenshot object...");
        result = new EyesWebDriverScreenshot(logger, getEyesDriver(), entireFrameOrElement,
                new RectangleSize(entireFrameOrElement.getWidth(), entireFrameOrElement.getHeight()));

        if (getHideCaret() && activeElement != null) {
            try {
                getEyesDriver().executeScript("arguments[0].focus();", activeElement);
            } catch (WebDriverException e) {
                logger.verbose("WARNING: Could not return focus to active element! " + e.getMessage());
            }
        }

        logger.verbose("Done");
        return result;
    }

    protected EyesScreenshot getFullPageScreenshot() {
        FrameChain originalFrameChain = getEyesDriver().getFrameChain().clone();
        EyesTargetLocator switchTo = (EyesTargetLocator) getEyesDriver().switchTo();

        ScaleProviderFactory scaleProviderFactory = updateScalingParams();
        FullPageCaptureAlgorithm algo = createFullPageCaptureAlgorithm(scaleProviderFactory);

        EyesWebDriverScreenshot result;

        Object activeElement = null;
        if (getHideCaret()) {
            try {
                activeElement = getEyesDriver().executeScript("var activeElement = document.activeElement; activeElement && activeElement.blur(); return activeElement;");
            } catch (WebDriverException e) {
                logger.verbose("WARNING: Cannot hide caret! " + e.getMessage());
            }
        }

        logger.verbose("Full page screenshot requested.");

        // Save the current frame path.
        Location originalFramePosition =
                originalFrameChain.size() > 0
                        ? originalFrameChain.getDefaultContentScrollPosition()
                        : Location.ZERO;

        switchTo.defaultContent();

        BufferedImage fullPageImage = algo.getStitchedRegion(Region.EMPTY, null, positionProviderHandler.get());

        switchTo.frames(originalFrameChain);
        result = new EyesWebDriverScreenshot(logger, getEyesDriver(), fullPageImage, null, originalFramePosition);

        if (getHideCaret() && activeElement != null) {
            try {
                getEyesDriver().executeScript("arguments[0].focus();", activeElement);
            } catch (WebDriverException e) {
                logger.verbose("WARNING: Could not return focus to active element! " + e.getMessage());
            }
        }

        logger.verbose("Done");
        return result;
    }

    protected EyesScreenshot getSimpleScreenshot() {

        ScaleProviderFactory scaleProviderFactory = updateScalingParams();

        EyesWebDriverScreenshot result;

        Object activeElement = null;
        if (getHideCaret()) {
            try {
                activeElement = getEyesDriver().executeScript("var activeElement = document.activeElement; activeElement && activeElement.blur(); return activeElement;");
            } catch (WebDriverException e) {
                logger.verbose("WARNING: Cannot hide caret! " + e.getMessage());
            }
        }

        ensureElementVisible(this.targetElement);

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
        result = new EyesWebDriverScreenshot(logger, getEyesDriver(), screenshotImage);

        if (getHideCaret() && activeElement != null) {
            try {
                getEyesDriver().executeScript("arguments[0].focus();", activeElement);
            } catch (WebDriverException e) {
                logger.verbose("WARNING: Could not return focus to active element! " + e.getMessage());
            }
        }

        logger.verbose("Done");
        return result;
    }

    @Override
    protected EyesScreenshot getScreenshot() {

        logger.verbose("getScreenshot()");

        EyesScreenshot result;

        if (checkFrameOrElement) {
            result = getFrameOrElementScreenshot();
        } else if (getConfig().getForceFullPageScreenshot() || stitchContent) {
            result = getFullPageScreenshot();
        } else {
            result = getSimpleScreenshot();
        }

        logger.verbose("Done!");
        return result;
    }



    private FullPageCaptureAlgorithm createFullPageCaptureAlgorithm(ScaleProviderFactory scaleProviderFactory) {
        return new FullPageCaptureAlgorithm(logger, regionPositionCompensation,
                getWaitBeforeScreenshots(), debugScreenshotsProvider, screenshotFactory,
                new ScrollPositionProvider(logger, getJsExecutor()),
                scaleProviderFactory,
                cutProviderHandler.get(),
                getStitchOverlap(),
                imageProvider);
    }

    @Override
    protected String getTitle() {
        if (!doNotGetTitle) {
            try {
                return getEyesDriver().getTitle();
            } catch (Exception ex) {
                logger.verbose("failed (" + ex.getMessage() + ")");
                doNotGetTitle = true;
            }
        }

        return "";
    }

    @Override
    protected String getInferredEnvironment() {
        String userAgent = getEyesDriver().getUserAgent();
        if (userAgent != null) {
            return "useragent:" + userAgent;
        }

        return null;
    }

    protected boolean isMobileDevice(WebDriver driver) {
        return false;
    }


    private WebElement getScrollRootElement(IScrollRootElementContainer scrollRootElementContainer) {
        if (!isMobileDevice(getEyesDriver())) {
            if (scrollRootElementContainer == null) {
                return getEyesDriver().findElement(By.tagName("html"));
            }
            WebElement scrollRootElement = scrollRootElementContainer.getScrollRootElement();
            if (scrollRootElement == null) {
                By scrollRootSelector = scrollRootElementContainer.getScrollRootSelector();
                scrollRootElement = getEyesDriver().findElement(scrollRootSelector != null ? scrollRootSelector : By.tagName("html"));
            }
            return scrollRootElement;
        }

        return null;
    }

    private PositionProvider getElementPositionProvider(WebElement scrollRootElement) {
        PositionProvider positionProvider = ((EyesRemoteWebElement) scrollRootElement).getPositionProvider();
        if (positionProvider == null) {
            positionProvider = createPositionProvider(scrollRootElement);
            ((EyesRemoteWebElement) scrollRootElement).setPositionProvider(positionProvider);
        }
        logger.verbose("position provider: " + positionProvider);
        currentFramePositionProvider = positionProvider;
        return positionProvider;
    }

    /**
     * @return The currently set position provider.
     */
    private PositionProvider getElementPositionProvider() {
        return elementPositionProvider == null ? positionProviderHandler.get() : elementPositionProvider;
    }

    @Override
    protected String getAUTSessionId() {
        try {
            return getEyesDriver().getRemoteWebDriver().getSessionId().toString();
        } catch (Exception e) {
            logger.log("WARNING: Failed to get AUT session ID! (maybe driver is not available?). Error: "
                    + e.getMessage());
            return "";
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    class EyesSeleniumAgentSetup {
        class WebDriverInfo {
            public String getName() {
                return remoteWebDriver.getClass().getName();
            }

            public Capabilities getCapabilities() {
                return remoteWebDriver.getCapabilities();
            }
        }

        public EyesSeleniumAgentSetup() {
            remoteWebDriver = getEyesDriver().getRemoteWebDriver();
        }

        private RemoteWebDriver remoteWebDriver;

        public String getSeleniumSessionId() {
            return remoteWebDriver.getSessionId().toString();
        }

        public WebDriverInfo getWebDriver() {
            return new WebDriverInfo();
        }

        public double getDevicePixelRatio() {
            return Eyes.this.getDevicePixelRatio();
        }

        public String getCutProvider() {
            return Eyes.this.cutProviderHandler.get().getClass().getName();
        }

        public String getScaleProvider() {
            return Eyes.this.scaleProviderHandler.get().getClass().getName();
        }

        public StitchMode getStitchMode() {
            return Eyes.this.getStitchMode();
        }

        public boolean getHideScrollbars() {
            return Eyes.this.getHideScrollbars();
        }

        public boolean getForceFullPageScreenshot() {
            return Eyes.this.getForceFullPageScreenshot();
        }
    }

    @Override
    protected Object getAgentSetup() {
        return new EyesSeleniumAgentSetup();
    }

    public IServerConnector getServerConnector() {
        return this.serverConnector;
    }

    @Override
    public boolean isSendDom() {
        return !isMobileDevice(getEyesDriver()) && super.isSendDom();
    }

    public Location getElementOriginalLocation(WebElement element) {
        return new Location(element.getLocation().getX(), element.getLocation().getY());
    }
}
