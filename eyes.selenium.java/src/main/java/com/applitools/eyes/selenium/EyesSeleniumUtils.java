/*
 * Applitools software.
 */
package com.applitools.eyes.selenium;

import com.applitools.eyes.*;
import com.applitools.eyes.selenium.exceptions.EyesDriverOperationException;
import com.applitools.eyes.selenium.wrappers.EyesWebDriver;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.internal.Coordinates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * We named this class EyesSeleniumUtils because there's a SeleniumUtils
 * class, and it caused collision.
 */
@SuppressWarnings("WeakerAccess")
public class EyesSeleniumUtils {

    private static final String NATIVE_APP = "NATIVE_APP";

    // See Applitools WiKi for explanation.
    private static final String JS_GET_VIEWPORT_SIZE =
            "var height = undefined;"
                    + "var width = undefined;"
                    + "  if (window.innerHeight) {height = window.innerHeight;}"
                    + "  else if (document.documentElement "
                    + "&& document.documentElement.clientHeight) "
                    + "{height = document.documentElement.clientHeight;}"
                    + "  else { var b = document.getElementsByTagName('body')[0]; "
                    + "if (b.clientHeight) {height = b.clientHeight;}"
                    + "};"
                    + " if (window.innerWidth) {width = window.innerWidth;}"
                    + " else if (document.documentElement "
                    + "&& document.documentElement.clientWidth) "
                    + "{width = document.documentElement.clientWidth;}"
                    + " else { var b = document.getElementsByTagName('body')[0]; "
                    + "if (b.clientWidth) {"
                    + "width = b.clientWidth;}"
                    + "};"
                    + "return [width, height];";

    private static final String JS_GET_CURRENT_SCROLL_POSITION =
            "var doc = document.documentElement; " +
                    "var x = window.scrollX || " +
                    "((window.pageXOffset || doc.scrollLeft) - (doc.clientLeft || 0));"
                    + " var y = window.scrollY || " +
                    "((window.pageYOffset || doc.scrollTop) - (doc.clientTop || 0));" +
                    "return [x, y];";


    // IMPORTANT: Notice there's a major difference between scrollWidth
    // and scrollHeight. While scrollWidth is the maximum between an
    // element's width and its content width, scrollHeight might be
    // smaller (!) than the clientHeight, which is why we take the
    // maximum between them.
    private static final String JS_GET_CONTENT_ENTIRE_SIZE =
            "var scrollWidth = document.documentElement.scrollWidth; " +
                    "var bodyScrollWidth = document.body.scrollWidth; " +
                    "var totalWidth = Math.max(scrollWidth, bodyScrollWidth); " +
                    "var clientHeight = document.documentElement.clientHeight; " +
                    "var bodyClientHeight = document.body.clientHeight; " +
                    "var scrollHeight = document.documentElement.scrollHeight; " +
                    "var bodyScrollHeight = document.body.scrollHeight; " +
                    "var maxDocElementHeight = Math.max(clientHeight, scrollHeight); " +
                    "var maxBodyHeight = Math.max(bodyClientHeight, bodyScrollHeight); " +
                    "var totalHeight = Math.max(maxDocElementHeight, maxBodyHeight); " +
                    "return [totalWidth, totalHeight];";

    private static final String[] JS_TRANSFORM_KEYS = {"transform",
            "-webkit-transform"
    };


    private static final String JS_GET_ENTIRE_PAGE_SIZE =
            "var width = Math.max(arguments[0].clientWidth, arguments[0].scrollWidth);" +
                    "var height = Math.max(arguments[0].clientHeight, arguments[0].scrollHeight);" +
                    "return [width, height];";

    /**
     * Extracts the location relative to the entire page from the coordinates
     * (e.g. as opposed to viewport)
     *
     * @param coordinates The coordinates from which location is extracted.
     * @return The location relative to the entire page
     */
    public static Location getPageLocation(Coordinates coordinates) {
        if (coordinates == null) {
            return null;
        }

        Point p = coordinates.onPage();
        return new Location(p.getX(), p.getY());
    }

    /**
     * Extracts the location relative to the <b>viewport</b> from the
     * coordinates (e.g. as opposed to the entire page).
     *
     * @param coordinates The coordinates from which location is extracted.
     * @return The location relative to the viewport.
     */
    public static Location getViewportLocation(Coordinates coordinates) {
        if (coordinates == null) {
            return null;
        }

        Point p = coordinates.inViewPort();
        return new Location(p.getX(), p.getY());
    }

    /**
     * For EyesWebDriver instances, returns the underlying WebDriver. For all other types - return the driver received
     * as parameter.
     *
     * @param driver The driver instance for which to get the underlying WebDriver.
     * @return The underlying WebDriver
     */
    public static WebDriver getUnderlyingDriver(WebDriver driver) {
        if (driver instanceof EyesWebDriver) {
            driver = ((EyesWebDriver) driver).getRemoteWebDriver();
        }

        return driver;
    }

    /**
     * @param driver The driver for which to check the orientation.
     * @return {@code true} if this is a mobile device and is in landscape
     * orientation. {@code false} otherwise.
     */
    public static boolean isLandscapeOrientation(Logger logger, WebDriver driver) {
        // We can only find orientation for mobile devices.
        return false;
    }


    @SuppressWarnings("unused")
    public static String selectRootElement(JavascriptExecutor executor) {
        // FIXME: 16/06/2018 HOTFIX for returning using documentElement as default for "hideScrollbars"
        //  (selection logic does not work).

//        String script =
//                "var docElemScrollHeightBefore = document.documentElement.scrollHeight; " +
//                "var originalBodyOverflow = document.body.style.overflow; " +
//                "document.body.style.overflow = 'hidden'; " +
//                "var docElemScrollHeightAfter = document.documentElement.scrollHeight; " +
//                "if (docElemScrollHeightBefore != docElemScrollHeightAfter) " +
//                "var retVal = 'documentElement'; " +
//                "else " +
//                "var retVal = 'body'; " +
//                "document.body.style.overflow = originalBodyOverflow; " +
//                "return retVal;";
//
//        return (String)executor.executeScript(script);
        return "documentElement";
    }

    /**
     * Sets the overflow of the current context's body.
     *
     * @param executor The executor to use for setting the overflow.
     * @param value    The overflow value to set.
     * @return The previous overflow value (could be {@code null} if undefined).
     */
    public static String setOverflow(JavascriptExecutor executor,
                                     String value,
                                     WebElement rootElement) {
        ArgumentGuard.notNull(executor, "executor");
        ArgumentGuard.notNull(rootElement, "rootElement");

        String script = String.format(
                "var origOF = arguments[0].style.overflow; " +
                        "arguments[0].style.overflow = '%s'; " +
                        "return origOF;", value);

        try {
            String result = (String) executor.executeScript(script, rootElement);
            GeneralUtils.sleep(200);
            return result;
        } catch (WebDriverException e) {
            throw new EyesDriverOperationException("Failed to set overflow", e);
        }
    }

    /**
     * @param executor The executor to use.
     * @return The current scroll position of the current frame.
     */
    public static Location getCurrentScrollPosition(
            IEyesJsExecutor executor) {
        //noinspection unchecked
        List<Number> positionAsList = (List<Number>) executor.executeScript(JS_GET_CURRENT_SCROLL_POSITION);
        return new Location((int) Math.ceil(positionAsList.get(0).doubleValue()),
                (int) Math.ceil(positionAsList.get(1).doubleValue()));
    }

    /**
     * Sets the scroll position of the current frame.
     *
     * @param executor The executor to use.
     * @param location The position to be set.
     */
    public static void setCurrentScrollPosition(IEyesJsExecutor executor,
                                                Location location) {
        executor.executeScript(String.format("window.scroll(%d,%d)",
                location.getX(), location.getY()));
    }

    /**
     * @param executor The executor to use.
     * @return The size of the entire content.
     */
    @SuppressWarnings("unchecked")
    public static RectangleSize getCurrentFrameContentEntireSize(
            IEyesJsExecutor executor) {
        RectangleSize result;
        try {
            //noinspection unchecked
            Object retVal = executor.executeScript(JS_GET_CONTENT_ENTIRE_SIZE);
            List<Long> esAsList = (List<Long>) retVal;
            result = new RectangleSize(esAsList.get(0).intValue(),
                    esAsList.get(1).intValue());
        } catch (WebDriverException e) {
            throw new EyesDriverOperationException(
                    "Failed to extract entire size!");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static RectangleSize getEntireElementSize(IEyesJsExecutor executor, WebElement element) {
        RectangleSize result;
        try {
            //noinspection unchecked
            Object retVal = executor.executeScript(JS_GET_ENTIRE_PAGE_SIZE, element);
            List<Long> esAsList = (List<Long>) retVal;
            result = new RectangleSize(esAsList.get(0).intValue(),
                    esAsList.get(1).intValue());
        } catch (WebDriverException e) {
            GeneralUtils.logExceptionStackTrace(e);
            throw new EyesDriverOperationException(
                    "Failed to extract entire size!");
        }
        return result;

    }

    /**
     * @param executor The executor to use.
     * @return The viewport size.
     */
    public static RectangleSize getViewportSize(
            JavascriptExecutor executor) {
        //noinspection unchecked
        List<Long> vsAsList =
                (List<Long>) executor.executeScript(JS_GET_VIEWPORT_SIZE);
        return new RectangleSize(vsAsList.get(0).intValue(),
                vsAsList.get(1).intValue());
    }

    /**
     * @param logger The logger to use.
     * @param driver The web driver to use.
     * @return The viewport size of the current context, or the display size
     * if the viewport size cannot be retrieved.
     */
    public static RectangleSize getViewportSizeOrDisplaySize(Logger logger, WebDriver driver) {
        logger.verbose("getViewportSizeOrDisplaySize()");

        try {
            return getViewportSize((JavascriptExecutor) driver);
        } catch (Exception ex) {
            logger.verbose(String.format(
                    "Failed to extract viewport size using Javascript: %s",
                    ex.getMessage()));
        }
        // If we failed to extract the viewport size using JS, will use the
        // window size instead.
        logger.verbose("Using window size as viewport size.");
        Dimension windowSize = driver.manage().window().getSize();
        int width = windowSize.getWidth();
        int height = windowSize.getHeight();
        try {
            if (EyesSeleniumUtils.isLandscapeOrientation(logger, driver) &&
                    height > width) {
                //noinspection SuspiciousNameCombination
                int height2 = width;
                //noinspection SuspiciousNameCombination
                width = height;
                height = height2;
            }
        } catch (WebDriverException e) {
            // Not every WebDriver supports querying for orientation.
        }
        logger.verbose(String.format("Done! Size %d x %d", width, height));
        return new RectangleSize(width, height);
    }

    public static boolean setBrowserSize(Logger logger, WebDriver driver,
                                         RectangleSize requiredSize) {
        final int SLEEP = 1000;
        int retriesLeft = 3;
        Dimension dRequiredSize = new Dimension(requiredSize.getWidth(), requiredSize.getHeight());
        Dimension dCurrentSize;
        RectangleSize currentSize;
        do {
            logger.verbose("Trying to set browser size to: " + requiredSize);
            driver.manage().window().setSize(dRequiredSize);
            GeneralUtils.sleep(SLEEP);
            dCurrentSize = driver.manage().window().getSize();
            currentSize = new RectangleSize(dCurrentSize.getWidth(),
                    dCurrentSize.getHeight());
            logger.verbose("Current browser size: " + currentSize);
        } while (--retriesLeft > 0 && !currentSize.equals(requiredSize));

        return currentSize == requiredSize;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean setBrowserSizeByViewportSize(Logger logger, WebDriver driver,
                                                       RectangleSize actualViewportSize,
                                                       RectangleSize requiredViewportSize) {
        Dimension browserSize = driver.manage().window().getSize();
        logger.verbose("Current browser size: " + browserSize);
        RectangleSize requiredBrowserSize = new RectangleSize(
                browserSize.width +
                        (requiredViewportSize.getWidth() - actualViewportSize.getWidth()),
                browserSize.height +
                        (requiredViewportSize.getHeight() - actualViewportSize.getHeight()));

        return setBrowserSize(logger, driver, requiredBrowserSize);
    }

    /**
     * @param logger The logger to use.
     * @param driver The web driver to use.
     * @param size   The size to set as the viewport size.
     */
    public static void setViewportSize(Logger logger, WebDriver driver, RectangleSize size) {

        ArgumentGuard.notNull(size, "size");

        logger.verbose("setViewportSize(" + size + ")");

        RectangleSize requiredSize = new RectangleSize(size.getWidth(), size.getHeight());
        RectangleSize actualViewportSize = getViewportSize((JavascriptExecutor) driver);
        logger.verbose("Initial viewport size:" + actualViewportSize);

        // If the viewport size is already the required size
        if (actualViewportSize.equals(requiredSize)) {
            logger.verbose("Required size already set.");
            return;
        }

        // We move the window to (0,0) to have the best chance to be able to
        // set the viewport size as requested.
        try {
            driver.manage().window().setPosition(new Point(0, 0));
        } catch (WebDriverException e) {
            logger.verbose("Warning: Failed to move the browser window to (0,0)");
        }

        setBrowserSizeByViewportSize(logger, driver, actualViewportSize, requiredSize);

        actualViewportSize = getViewportSize((JavascriptExecutor) driver);

        if (actualViewportSize.equals(requiredSize)) {
            return;
        }

        // Additional attempt. This Solves the "maximized browser" bug
        // (border size for maximized browser sometimes different than
        // non-maximized, so the original browser size calculation is
        // wrong).
        logger.verbose("Trying workaround for maximization...");
        setBrowserSizeByViewportSize(logger, driver, actualViewportSize, requiredSize);

        actualViewportSize = getViewportSize((JavascriptExecutor) driver);
        logger.verbose("Current viewport size: " + actualViewportSize);

        if (actualViewportSize.equals(requiredSize)) {
            return;
        }

        final int MAX_DIFF = 3;
        int widthDiff = actualViewportSize.getWidth() - requiredSize.getWidth();
        int widthStep = widthDiff > 0 ? -1 : 1; // -1 for smaller size, 1 for larger
        int heightDiff = actualViewportSize.getHeight() - requiredSize.getHeight();
        int heightStep = heightDiff > 0 ? -1 : 1;

        Dimension dBrowserSize = driver.manage().window().getSize();
        RectangleSize browserSize = new RectangleSize(dBrowserSize.getWidth(),
                dBrowserSize.getHeight());

        int currWidthChange = 0;
        int currHeightChange = 0;
        // We try the zoom workaround only if size difference is reasonable.
        if (Math.abs(widthDiff) <= MAX_DIFF && Math.abs(heightDiff) <= MAX_DIFF) {
            logger.verbose("Trying workaround for zoom...");
            int retriesLeft = Math.abs((widthDiff == 0 ? 1 : widthDiff) * (heightDiff == 0 ? 1 : heightDiff)) * 2;
            RectangleSize lastRequiredBrowserSize = null;
            do {
                logger.verbose("Retries left: " + retriesLeft);
                // We specifically use "<=" (and not "<"), so to give an extra resize attempt
                // in addition to reaching the diff, due to floating point issues.
                if (Math.abs(currWidthChange) <= Math.abs(widthDiff) &&
                        actualViewportSize.getWidth() != requiredSize.getWidth()) {
                    currWidthChange += widthStep;
                }
                if (Math.abs(currHeightChange) <= Math.abs(heightDiff) &&
                        actualViewportSize.getHeight() != requiredSize.getHeight()) {
                    currHeightChange += heightStep;
                }

                RectangleSize requiredBrowserSize = new RectangleSize(browserSize.getWidth() + currWidthChange,
                        browserSize.getHeight() + currHeightChange);
                if (requiredBrowserSize.equals(lastRequiredBrowserSize)) {
                    logger.verbose("Browser size is as required but viewport size does not match!");
                    logger.verbose("Browser size: " + requiredBrowserSize + " , Viewport size: " + actualViewportSize);
                    logger.verbose("Stopping viewport size attempts.");
                    break;
                }

                setBrowserSize(logger, driver, requiredBrowserSize);
                lastRequiredBrowserSize = requiredBrowserSize;

                actualViewportSize = getViewportSize((JavascriptExecutor) driver);
                logger.verbose("Current viewport size: " + actualViewportSize);

                if (actualViewportSize.equals(requiredSize)) {
                    return;
                }
            } while ((Math.abs(currWidthChange) <= Math.abs(widthDiff) ||
                    Math.abs(currHeightChange) <= Math.abs(heightDiff))
                    && (--retriesLeft > 0));

            logger.verbose("Zoom workaround failed.");
        }

        throw new EyesException("Failed to set viewport size!");
    }

    /**
     * @param executor The executor to use.
     * @return The device pixel ratio.
     */
    public static float getDevicePixelRatio(IEyesJsExecutor executor) {
        return Float.parseFloat(
                executor.executeScript("return window.devicePixelRatio")
                        .toString());
    }

    /**
     * @param executor The executor to use.
     * @return The current documentElement transform values, according to
     * {@link #JS_TRANSFORM_KEYS}.
     */
    public static Map<String, String> getCurrentTransform(IEyesJsExecutor executor) {

        StringBuilder script = new StringBuilder("return { ");

        for (String key : JS_TRANSFORM_KEYS) {
            script.append("'").append(key).append("'").append(": document.documentElement.style['").append(key).append("'],");
        }

        // Ending the list
        script.append(" }");

        //noinspection unchecked
        return (Map<String, String>) executor.executeScript(script.toString());

    }

    /**
     * Sets transforms for document.documentElement according to the given
     * map of style keys and values.
     *
     * @param executor   The executor to use.
     * @param transforms The transforms to set. Keys are used as style keys,
     *                   and values are the values for those styles.
     */
    public static void setTransforms(IEyesJsExecutor executor,
                                     Map<String, String> transforms) {

        StringBuilder script = new StringBuilder();

        for (Map.Entry<String, String> entry : transforms.entrySet()) {
            script.append("document.documentElement.style['").append(entry.getKey()).append("'] = '").append(entry.getValue()).append("';");
        }

        executor.executeScript(script.toString());
    }

    /**
     * Set the given transform to document.documentElement for all style keys
     * defined in {@link #JS_TRANSFORM_KEYS} .
     *
     * @param executor  The executor to use.
     * @param transform The transform value to set.
     */
    public static void setTransform(IEyesJsExecutor executor,
                                    String transform) {
        Map<String, String> transforms = new HashMap<>(JS_TRANSFORM_KEYS.length);

        for (String key : JS_TRANSFORM_KEYS) {
            transforms.put(key, transform);
        }

        setTransforms(executor, transforms);
    }

    /**
     * Translates the current documentElement to the given position.
     *
     * @param executor The executor to use.
     * @param position The position to translate to.
     */
    public static void translateTo(IEyesJsExecutor executor,
                                   Location position) {
        setTransform(executor, String.format("translate(-%spx, -%spx)",
                position.getX(), position.getY()));
    }

    /**
     * Returns given element visible portion size.
     *
     * @param element The element for which to return the size.
     * @return The given element's visible portion size.
     */
    public static RectangleSize getElementVisibleSize(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        Region region = new Region(location.getX(), location.getY(), size.getWidth(), size.getHeight());
        WebElement parent;

        try {
            parent = element.findElement(By.xpath(".."));
        } catch (Exception e) {
            parent = null;
        }

        try {
            while (parent != null && !region.isSizeEmpty()) {
                Point parentLocation = parent.getLocation();
                Dimension parentSize = parent.getSize();
                Region parentRegion = new Region(parentLocation.getX(), parentLocation.getY(),
                        parentSize.getWidth(), parentSize.getHeight());

                region.intersect(parentRegion);
                try {
                    parent = parent.findElement(By.xpath(".."));
                } catch (Exception e) {
                    parent = null;
                }
            }
        } catch (Exception ex) {
            GeneralUtils.logExceptionStackTrace(ex);
        }

        return region.getSize();
    }
}
