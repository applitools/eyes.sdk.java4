/*
 * Applitools SDK for Selenium integration.
 */
package com.applitools.eyes;

import com.applitools.eyes.capture.AppOutputProvider;
import com.applitools.eyes.capture.AppOutputWithScreenshot;
import com.applitools.eyes.fluent.GetFloatingRegion;
import com.applitools.eyes.fluent.GetRegion;
import com.applitools.eyes.fluent.ICheckSettingsInternal;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchWindowTask {

    private static final int MATCH_INTERVAL = 500; // Milliseconds

    private final Logger logger;
    private final IServerConnector serverConnector;
    private final RunningSession runningSession;
    private final int defaultRetryTimeout;
    private final AppOutputProvider appOutputProvider;

    private EyesScreenshot lastScreenshot = null;
    private MatchResult matchResult;
    private Region lastScreenshotBounds;
    private EyesBase eyes;

    /**
     * @param logger            A logger instance.
     * @param serverConnector   Our gateway to the agent
     * @param runningSession    The running session in which we should match the window
     * @param retryTimeout      The default total time to retry matching (ms).
     * @param eyes              An EyesBase object.
     * @param appOutputProvider A callback for getting the application output when performing match.
     */
    public MatchWindowTask(Logger logger, IServerConnector serverConnector,
                           RunningSession runningSession, int retryTimeout,
                           EyesBase eyes, AppOutputProvider appOutputProvider) {
        ArgumentGuard.notNull(serverConnector, "serverConnector");
        ArgumentGuard.notNull(runningSession, "runningSession");
        ArgumentGuard.greaterThanOrEqualToZero(retryTimeout, "retryTimeout");
        ArgumentGuard.notNull(appOutputProvider, "appOutputProvider");

        this.logger = logger;
        this.serverConnector = serverConnector;
        this.runningSession = runningSession;
        this.defaultRetryTimeout = retryTimeout;
        this.eyes = eyes;
        this.appOutputProvider = appOutputProvider;
    }

    /**
     * @param logger            A logger instance.
     * @param serverConnector   Our gateway to the agent
     * @param runningSession    The running session in which we should match the window
     * @param retryTimeout      The default total time to retry matching (ms).
     */
    public MatchWindowTask(Logger logger, IServerConnector serverConnector,
                           RunningSession runningSession, int retryTimeout,
                           EyesBase eyes) {
        ArgumentGuard.notNull(serverConnector, "serverConnector");
        ArgumentGuard.notNull(runningSession, "runningSession");
        ArgumentGuard.greaterThanOrEqualToZero(retryTimeout, "retryTimeout");

        this.logger = logger;
        this.serverConnector = serverConnector;
        this.runningSession = runningSession;
        this.defaultRetryTimeout = retryTimeout;
        this.eyes = eyes;
        this.appOutputProvider = null;
    }
    /**
     * Creates the match data and calls the server connector matchWindow method.
     * @param userInputs         The user inputs related to the current appOutput.
     * @param appOutput          The application output to be matched.
     * @param tag                Optional tag to be associated with the match (can be {@code null}).
     * @param ignoreMismatch     Whether to instruct the server to ignore the match attempt in case of a mismatch.
     * @param imageMatchSettings The settings to use.
     * @return The match result.
     */
    public MatchResult performMatch(Trigger[] userInputs,
                                     AppOutputWithScreenshot appOutput,
                                     String tag, boolean ignoreMismatch,
                                     ImageMatchSettings imageMatchSettings) {

        String agentSetupStr = "";
        if (eyes != null) {
            Object agentSetup = eyes.getAgentSetup();
            ObjectMapper jsonMapper = new ObjectMapper();
            try {
                agentSetupStr = jsonMapper.writeValueAsString(agentSetup);
            } catch (JsonProcessingException e) {
                GeneralUtils.logExceptionStackTrace(logger, e);
            }
        }

        // Prepare match data.
        MatchWindowData data = new MatchWindowData(
                userInputs,
                appOutput.getAppOutput(),
                tag,
                ignoreMismatch,
                new MatchWindowData.Options(tag, userInputs, ignoreMismatch,
                        false, false, false,
                        imageMatchSettings),
                agentSetupStr);

        if (!tryUploadImage(data)) {
            throw new EyesException("matchWindow failed: could not upload image to storage service.");
        }

        // Perform match.
        return serverConnector.matchWindow(runningSession, data);
    }

    private boolean tryUploadImage(MatchWindowData data) {
        AppOutput appOutput = data.getAppOutput();
        if (appOutput.getScreenshotUrl() != null) {
            return true;
        }

        // Getting the screenshot's bytes
        byte[] bytes = appOutput.getScreenshotBytes();
        String targetUrl = tryUploadData(bytes, "image/png", "image/png");
        appOutput.setScreenshotUrl(targetUrl);
        return targetUrl != null;
    }

    public String tryUploadData(byte[] bytes, String contentType, String mediaType) {
        String targetUrl;

        RenderingInfo renderingInfo = serverConnector.getRenderInfo();
        if (renderingInfo != null && (targetUrl = renderingInfo.getResultsUrl()) != null) {
            try {
                UUID uuid = UUID.randomUUID();
                targetUrl = targetUrl.replace("__random__", uuid.toString());
                logger.verbose("uploading " + mediaType + " to " + targetUrl);

                int retriesLeft = 3;
                int wait = 500;
                while (retriesLeft-- > 0) {
                    try {
                        int statusCode = serverConnector.uploadData(bytes, renderingInfo, targetUrl, contentType, mediaType);
                        if (statusCode == 200 || statusCode == 201) {
                            logger.verbose("upload " + mediaType + " guid " + uuid + "complete.");
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
                logger.log("Error uploading " + mediaType);
                GeneralUtils.logExceptionStackTrace(logger, e);
            }
        }
        return null;
    }

    /**
     * Repeatedly obtains an application snapshot and matches it with the next
     * expected output, until a match is found or the timeout expires.
     * @param userInputs             User input preceding this match.
     * @param region                 Window region to capture.
     * @param tag                    Optional tag to be associated with the match (can be {@code null}).
     * @param shouldRunOnceOnTimeout Force a single match attempt at the end of the match timeout.
     * @param ignoreMismatch         Whether to instruct the server to ignore the match attempt in case of a mismatch.
     * @param checkSettingsInternal     The settings to use.
     * @param retryTimeout           The amount of time to retry matching in milliseconds or a
     *                               negative value to use the default retry timeout.
     * @return Returns the results of the match
     */
    public MatchResult matchWindow(Trigger[] userInputs,
                                   Region region, String tag,
                                   boolean shouldRunOnceOnTimeout,
                                   boolean ignoreMismatch,
                                   ICheckSettingsInternal checkSettingsInternal,
                                   int retryTimeout) {

        if (retryTimeout < 0) {
            retryTimeout = defaultRetryTimeout;
        }

        logger.verbose(String.format("retryTimeout = %d", retryTimeout));

        EyesScreenshot screenshot = takeScreenshot(userInputs, region, tag,
                shouldRunOnceOnTimeout, ignoreMismatch, checkSettingsInternal, retryTimeout);

        if (ignoreMismatch) {
            return matchResult;
        }

        updateLastScreenshot(screenshot);
        updateBounds(region);

        return matchResult;
    }

    private void collectSimpleRegions(ICheckSettingsInternal checkSettingsInternal,
                                      ImageMatchSettings imageMatchSettings,
                                      EyesScreenshot screenshot) {

        imageMatchSettings.setIgnoreRegions(collectRegions(checkSettingsInternal.getIgnoreRegions(), screenshot, false));
        imageMatchSettings.setLayoutRegions(collectRegions(checkSettingsInternal.getLayoutRegions(), screenshot, true));
        imageMatchSettings.setStrictRegions(collectRegions(checkSettingsInternal.getStrictRegions(), screenshot, true));
        imageMatchSettings.setContentRegions(collectRegions(checkSettingsInternal.getContentRegions(), screenshot, true));
    }

    private Region[] collectRegions(GetRegion[] regionProviders, EyesScreenshot screenshot, boolean adjustLocation) {

        List<Region> regions = new ArrayList<>();
        for (GetRegion regionProvider : regionProviders) {
            try {
                regions.addAll(regionProvider.getRegions(eyes, screenshot, adjustLocation));
            }
            catch (OutOfBoundsException ex){
                logger.log("WARNING - ignore region was out of bounds.");
            }
        }
        return regions.toArray(new Region[0]);
    }

    private void collectFloatingRegions(ICheckSettingsInternal checkSettingsInternal,
                                        ImageMatchSettings imageMatchSettings,
                                        EyesScreenshot screenshot) {
        List<FloatingMatchSettings> floatingRegions = new ArrayList<>();
        for (GetFloatingRegion floatingRegionProvider : checkSettingsInternal.getFloatingRegions()) {
            floatingRegions.addAll(floatingRegionProvider.getRegions(eyes, screenshot));
        }
        imageMatchSettings.setFloatingRegions(floatingRegions.toArray(new FloatingMatchSettings[0]));
    }

    /**
     * Build match settings by merging the check settings and the default match settings.
     * @param checkSettingsInternal the settings to match the image by.
     * @param screenshot the Screenshot wrapper object.
     * @return Merged match settings.
     */
    public ImageMatchSettings createImageMatchSettings(ICheckSettingsInternal checkSettingsInternal, EyesScreenshot screenshot, EyesBase eyes) {
        ImageMatchSettings imageMatchSettings = createImageMatchSettings(checkSettingsInternal, eyes);
        if (imageMatchSettings != null) {
            collectSimpleRegions(checkSettingsInternal, imageMatchSettings, screenshot);
            collectFloatingRegions(checkSettingsInternal, imageMatchSettings, screenshot);
            collectAccessibilityRegions(checkSettingsInternal, imageMatchSettings, eyes, screenshot);
        }
        return imageMatchSettings;
    }

    public static ImageMatchSettings createImageMatchSettings(ICheckSettingsInternal checkSettingsInternal, EyesBase eyes) {
        ImageMatchSettings imageMatchSettings = null;
        if (checkSettingsInternal != null) {
            MatchLevel matchLevel = checkSettingsInternal.getMatchLevel();
            if (matchLevel == null) {
                matchLevel = eyes.getDefaultMatchSettings().getMatchLevel();
            }

            imageMatchSettings = new ImageMatchSettings(matchLevel, null, false);

            Boolean ignoreCaret = checkSettingsInternal.getIgnoreCaret();
            if (ignoreCaret == null) {
                ignoreCaret = eyes.getDefaultMatchSettings().getIgnoreCaret();
            }

            imageMatchSettings.setIgnoreCaret(ignoreCaret);
            imageMatchSettings.setUseDom(checkSettingsInternal.isUseDom() != null ? checkSettingsInternal.isUseDom() : false);
            imageMatchSettings.setAccessibilitySettings(eyes.getDefaultMatchSettings().getAccessibilitySettings());
            imageMatchSettings.setIgnoreDisplacements(checkSettingsInternal.isIgnoreDisplacements());
            imageMatchSettings.setEnablePatterns(checkSettingsInternal.isEnablePatterns());
        }
        return imageMatchSettings;
    }

    private EyesScreenshot takeScreenshot(Trigger[] userInputs, Region region, String tag,
                                          boolean shouldMatchWindowRunOnceOnTimeout,
                                          boolean ignoreMismatch, ICheckSettingsInternal checkSettingsInternal,
                                          int retryTimeout) {
        long elapsedTimeStart = System.currentTimeMillis();
        EyesScreenshot screenshot;

        // If the wait to load time is 0, or "run once" is true,
        // we perform a single check window.
        if (0 == retryTimeout || shouldMatchWindowRunOnceOnTimeout) {

            if (shouldMatchWindowRunOnceOnTimeout) {
                GeneralUtils.sleep(retryTimeout);
            }
            screenshot = tryTakeScreenshot(userInputs, region, tag, ignoreMismatch, checkSettingsInternal);
        } else {
            screenshot = retryTakingScreenshot(userInputs, region, tag, ignoreMismatch, checkSettingsInternal,
                    retryTimeout);
        }

        double elapsedTime = (System.currentTimeMillis() - elapsedTimeStart) / 1000;
        logger.verbose(String.format("Completed in %.2f seconds", elapsedTime));
        //matchResult.setScreenshot(screenshot);
        return screenshot;
    }

    private EyesScreenshot retryTakingScreenshot(Trigger[] userInputs, Region region, String tag, boolean ignoreMismatch,
                                                 ICheckSettingsInternal checkSettingsInternal, int retryTimeout) {
        // Start the retry timer.
        long start = System.currentTimeMillis();

        EyesScreenshot screenshot = null;

        long retry = System.currentTimeMillis() - start;

        // The match retry loop.
        while (retry < retryTimeout) {

            // Wait before trying again.
            GeneralUtils.sleep(MATCH_INTERVAL);

            screenshot = tryTakeScreenshot(userInputs, region, tag, true, checkSettingsInternal);

            if (matchResult.getAsExpected()) {
                break;
            }

            retry = System.currentTimeMillis() - start;
        }

        // if we're here because we haven't found a match yet, try once more
        if (!matchResult.getAsExpected()) {
            screenshot = tryTakeScreenshot(userInputs, region, tag, ignoreMismatch, checkSettingsInternal);
        }
        return screenshot;
    }

    private EyesScreenshot tryTakeScreenshot(Trigger[] userInputs, Region region, String tag,
                                             boolean ignoreMismatch, ICheckSettingsInternal checkSettingsInternal) {
        AppOutputWithScreenshot appOutput = appOutputProvider.getAppOutput(region, lastScreenshot, checkSettingsInternal);
        EyesScreenshot screenshot = appOutput.getScreenshot();
        ImageMatchSettings matchSettings = createImageMatchSettings(checkSettingsInternal, screenshot, eyes);
        matchResult = performMatch(userInputs, appOutput, tag, ignoreMismatch, matchSettings);
        return screenshot;
    }

    private void updateLastScreenshot(EyesScreenshot screenshot) {
        if (screenshot != null) {
            lastScreenshot = screenshot;
        }
    }

    private void updateBounds(Region region) {
        if (region.isSizeEmpty()) {
            if (lastScreenshot == null) {
                // We set an "infinite" image size since we don't know what the screenshot size is...
                lastScreenshotBounds = new Region(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            } else {
                BufferedImage image = lastScreenshot.getImage();
                lastScreenshotBounds = new Region(0, 0, image.getWidth(), image.getHeight());
            }
        } else {
            lastScreenshotBounds = region;
        }
    }

    public Region getLastScreenshotBounds() {
        return lastScreenshotBounds;
    }

    private static void collectAccessibilityRegions(ICheckSettingsInternal checkSettingsInternal,
                                                    ImageMatchSettings imageMatchSettings, EyesBase eyes,
                                                    EyesScreenshot screenshot) {
        List<AccessibilityRegionByRectangle> accessibilityRegions = new ArrayList<>();
        for (IGetAccessibilityRegion regionProvider : checkSettingsInternal.getAccessibilityRegions()) {
            accessibilityRegions.addAll(regionProvider.getRegions(eyes, screenshot));
        }
        imageMatchSettings.setAccessibility(accessibilityRegions.toArray(new AccessibilityRegionByRectangle[0]));

    }

}
