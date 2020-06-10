/*
 * Applitools SDK for Selenium integration.
 */
package com.applitools.eyes.images;

import com.applitools.eyes.*;
import com.applitools.eyes.events.ValidationInfo;
import com.applitools.eyes.events.ValidationResult;
import com.applitools.eyes.exceptions.TestFailedException;
import com.applitools.eyes.fluent.CheckSettings;
import com.applitools.eyes.fluent.ICheckSettings;
import com.applitools.eyes.positioning.NullRegionProvider;
import com.applitools.eyes.positioning.RegionProvider;
import com.applitools.eyes.triggers.MouseAction;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.ImageUtils;

import java.awt.image.BufferedImage;

public class Eyes extends EyesBase {

    private String title;
    private EyesImagesScreenshot screenshot;
    private String inferred;

    /**
     * Get the base agent id.
     * @return Base agent id.
     */
    @Override
    public String getBaseAgentId() {
        return "eyes.images.java/4.13.1";
    }

    @Override
    public String tryCaptureDom() {
        return null;
    }

    /**
     * Starts a test.
     * @param appName    The name of the application under test.
     * @param testName   The test name.
     * @param dimensions Determines the resolution used for the baseline.
     *                   {@code null} will automatically grab the resolution from the image.
     */
    public void open(String appName, String testName,
                     RectangleSize dimensions) {
        config.setAppName(appName);
        config.setTestName(testName);
        config.setViewportSize(dimensions);
        openBase();
    }

    /**
     * ﻿Starts a new test without setting the viewport size of the AUT.
     * @param appName  The name of the application under test.
     * @param testName The test name.
     * @see #open(String, String, RectangleSize)
     */
    public void open(String appName, String testName) {
        config.setAppName(appName);
        config.setTestName(testName);
        openBase();
    }

    public boolean check(String name, ICheckSettings checkSettings) {
        IImagesCheckTarget imagesCheckTarget = (checkSettings instanceof IImagesCheckTarget) ? (IImagesCheckTarget) checkSettings : null;
        BufferedImage image = imagesCheckTarget.getImage();

        if (viewportSizeHandler.get() == null) {
            setViewportSize(new RectangleSize(image.getWidth(), image.getHeight()));
        }

        return checkImage_(NullRegionProvider.INSTANCE, image, name, false, checkSettings);
    }

    /**
     * Superseded by {@link #checkImage(java.awt.image.BufferedImage)}.
     */
    @Deprecated
    public boolean checkWindow(BufferedImage image) {
        return checkImage(image);
    }

    /** Superseded by {@link #checkImage(java.awt.image.BufferedImage, String)}.
     */
    @Deprecated
    public boolean checkWindow(BufferedImage image, String tag) {
        return checkImage(image, tag);
    }

    /**
     * Superseded by {@link #checkImage(java.awt.image.BufferedImage, String,
     * boolean)}.
     */
    @Deprecated
    public boolean checkWindow(BufferedImage image, String tag,
                               boolean ignoreMismatch) {
        return checkImage(image, tag, ignoreMismatch);
    }

    /**
     * See {@link #checkImage(BufferedImage, String)}.
     * {@code tag} defaults to {@code null}.
     */
    public boolean checkImage(BufferedImage image) {
        return checkImage(image, null);
    }

    /**
     * See {@link #checkImage(BufferedImage, String, boolean)}.
     * {@code ignoreMismatch} defaults to {@code false}.
     */
    public boolean checkImage(BufferedImage image, String tag) {
        return checkImage(image, tag, false);
    }

    /**
     * Matches the input image with the next expected image.
     * @param image          The image to perform visual validation for.
     * @param tag            An optional tag to be associated with the validation checkpoint.
     * @param ignoreMismatch True if the server should ignore a negative result for the visual validation.
     * @return True if the image matched the expected output, false otherwise.
     * @throws TestFailedException Thrown if a mismatch is detected and immediate failure reports are enabled.
     */
    public boolean checkImage(BufferedImage image, String tag,
                              boolean ignoreMismatch) {
        if (getIsDisabled()) {
            logger.verbose(String.format("CheckImage(Image, '%s', %b): Ignored", tag, ignoreMismatch));
            return false;
        }
        ArgumentGuard.notNull(image, "image cannot be null!");

        logger.verbose(String.format("CheckImage(Image, '%s', %b)", tag, ignoreMismatch));

        if (viewportSizeHandler.get() == null) {
            setViewportSize(new RectangleSize(image.getWidth(), image.getHeight()));
        }

        return checkImage_(NullRegionProvider.INSTANCE, image, tag, ignoreMismatch, new CheckSettings(USE_DEFAULT_TIMEOUT));
    }

    /**
     * See {@link #checkImage(String, String)}.
     * {@code tag} defaults to {@code null}.
     */
    public boolean checkImage(String path) {
        return checkImage(path, null);
    }

    /**
     * See {@link #checkImage(String, String, boolean)}.
     * {@code ignoreMismatch} defaults to {@code false}.
     * @param path The path to the image to check.
     * @param tag  The tag to be associated with the visual checkpoint.
     * @return Whether or not the image matched the baseline.
     */
    public boolean checkImage(String path, String tag) {
        return checkImage(path, tag, false);
    }

    /**
     * Matches the image stored in the input file with the next expected image.
     * <p>
     * See {@link #checkImage(BufferedImage, String, boolean)}.
     * @param path           The base64 representation of the image's raw bytes.
     * @param tag            An optional tag to be associated with the validation checkpoint.
     * @param ignoreMismatch True if the server should ignore a negative result for the visual validation.
     * @return Whether or not the image matched the baseline.
     */
    public boolean checkImage(String path, String tag, boolean ignoreMismatch) {
        return checkImage(ImageUtils.imageFromFile(path), tag, ignoreMismatch);
    }

    /**
     * See {@link #checkImage(byte[], String)}.
     * {@code tag} defaults to {@code null}.
     * @param image The raw png bytes of the image to perform visual validation for.
     * @return Whether or not the image matched the baseline.
     */
    public boolean checkImage(byte[] image) {
        return checkImage(image, null);
    }

    /**
     * See {@link #checkImage(byte[], String, boolean)}.
     * {@code ignoreMismatch} defaults to {@code false}.
     * @param image The raw png bytes of the image to perform visual validation for.
     * @param tag   An optional tag to be associated with the validation checkpoint.
     * @return Whether or not the image matched the baseline.
     */
    public boolean checkImage(byte[] image, String tag) {
        return checkImage(image, tag, false);
    }

    /**
     * Matches the input image with the next expected image.
     * See {@link #checkImage(BufferedImage, String, boolean)}.
     * @param image The raw png bytes of the image to perform visual validation for.
     * @param tag   An optional tag to be associated with the validation checkpoint.
     * @return Whether or not the image matched the baseline.
     */
    public boolean checkImage(byte[] image, String tag, boolean ignoreMismatch) {
        return checkImage(ImageUtils.imageFromBytes(image), tag, ignoreMismatch);
    }

    /**
     * Perform visual validation for the current image.
     * @param image          The image to perform visual validation for.
     * @param region         The region to validate within the image.
     * @param tag            An optional tag to be associated with the validation checkpoint.
     * @param ignoreMismatch True if the server should ignore a negative result for the visual validation.
     * @return Whether or not the image matched the baseline.
     * @throws TestFailedException Thrown if a mismatch is detected and immediate failure reports are enabled.
     */
    public boolean checkRegion(BufferedImage image, final Region region, String tag, boolean ignoreMismatch) {
        if (getIsDisabled()) {
            logger.verbose(String.format(
                    "CheckRegion(Image, [%s], '%s', %b): Ignored",
                    region, tag, ignoreMismatch));
            return false;
        }
        ArgumentGuard.notNull(image, "image cannot be null!");
        ArgumentGuard.notNull(region, "region cannot be null!");

        logger.verbose(String.format("CheckRegion(Image, [%s], '%s', %b)", region, tag, ignoreMismatch));

        if (viewportSizeHandler.get() == null) {
            setViewportSize(new RectangleSize(image.getWidth(), image.getHeight()));
        }

        return checkImage_(new RegionProvider() {
            public Region getRegion() {
                return region;
            }
        }, image, tag, ignoreMismatch, new CheckSettings(USE_DEFAULT_TIMEOUT));
    }

    /**
     * Perform visual validation for a region in a given image. Does not
     * ignore mismatches.
     * @param image  The image to perform visual validation for.
     * @param region The region to validate within the image.
     * @param tag    An optional tag to be associated with the validation checkpoint.
     * @throws TestFailedException Thrown if a mismatch is detected and immediate failure reports are enabled.
     */
    public void checkRegion(BufferedImage image, Region region, String tag) {
        checkRegion(image, region, tag, false);
    }

    /**
     * Perform visual validation of a region for a given image. Tag is empty and mismatches are not ignored.
     * @param image  The image to perform visual validation for.
     * @param region The region to validate within the image.
     * @throws TestFailedException Thrown if a mismatch is detected and immediate failure reports are enabled.
     */
    public void checkRegion(BufferedImage image, Region region) {
        checkRegion(image, region, null, false);
    }

    /**
     * Adds a mouse trigger.
     * @param action  Mouse action.
     * @param control The control on which the trigger is activated (context
     *                relative coordinates).
     * @param cursor  The cursor's position relative to the control.
     */
    public void addMouseTrigger(MouseAction action, Region control, Location cursor) {
        addMouseTriggerBase(action, control, cursor);
    }

    /**
     * Adds a keyboard trigger.
     * @param control The control's context-relative region.
     * @param text    The trigger's text.
     */
    public void addTextTrigger(Region control, String text) {
        addTextTriggerBase(control, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RectangleSize getViewportSize() {
        return viewportSizeHandler.get();
    }

    /**
     * Set the viewport size.
     * @param size The required viewport size.
     */
    @Override
    public void setViewportSize(RectangleSize size) {
        ArgumentGuard.notNull(size, "size");
        viewportSizeHandler.set(new RectangleSize(size.getWidth(), size.getHeight()));
    }

    /**
     * Get the inferred environment.
     * @return Inferred environment.
     */
    @Override
    protected String getInferredEnvironment() {
        return inferred != null ? inferred : "";
    }

    /**
     * Sets the inferred environment for the test.
     * @param inferred The inferred environment string.
     */
    public void setInferredEnvironment(String inferred) {
        this.inferred = inferred;
    }

    /**
     * Get the screenshot.
     * @return The screenshot.
     */
    @Override
    public EyesScreenshot getScreenshot() {
        return screenshot;
    }

    /**
     * Get the title.
     * @return The title.
     */
    @Override
    protected String getTitle() {
        return title;
    }

    @Override
    protected String getAUTSessionId() {
        return "";
    }

    /**
     * See {@link #checkImage_(RegionProvider, String, boolean, ICheckSettings)}.
     * @param regionProvider The region for which verification will be
     *                       performed. see {@link #checkWindowBase(RegionProvider, String, boolean, int)}.
     * @param image          The image to perform visual validation for.
     * @param tag            An optional tag to be associated with the validation checkpoint.
     * @param ignoreMismatch True if the server should ignore a negative result for the visual validation.
     * @param checkSettings  The settings to use when checking the image.
     * @return True if the image matched the expected output, false otherwise.
     */
    private boolean checkImage_(RegionProvider regionProvider,
                                BufferedImage image,
                                String tag,
                                boolean ignoreMismatch,
                                ICheckSettings checkSettings) {
        // We verify that the image is indeed in the correct format.
        image = ImageUtils.normalizeImageType(image);

        CutProvider cutProvider = cutProviderHandler.get();
        if (!(cutProvider instanceof NullCutProvider)) {
            logger.verbose("cutting...");
            image = cutProvider.cut(image);
            debugScreenshotsProvider.save(image, "cut");
        }

        // Set the screenshot to be verified.
        screenshot = new EyesImagesScreenshot(logger, image);

        return checkImage_(regionProvider, tag, ignoreMismatch, checkSettings);
    }


    /**
     * Internal function for performing an image verification for a region of
     * a buffered image.
     * @param regionProvider The region for which verification will be
     *                       performed. see {@link #checkWindowBase(RegionProvider, String, boolean, int)}.
     * @param tag            An optional tag to be associated with the validation checkpoint.
     * @param ignoreMismatch True if the server should ignore a negative result for the visual validation.
     * @param checkSettings  The settings to use when checking the image.
     * @return True if the image matched the expected output, false otherwise.
     */
    private boolean checkImage_(RegionProvider regionProvider, String tag, boolean ignoreMismatch, ICheckSettings checkSettings) {

        // Set the title to be linked to the screenshot.
        title = (tag != null) ? tag : "";

        ValidationInfo validationInfo = this.fireValidationWillStartEvent(tag);

        MatchResult result = checkWindowBase(regionProvider, tag, ignoreMismatch, checkSettings);

        ValidationResult validationResult = new ValidationResult();
        validationResult.setAsExpected(result.getAsExpected());

        getSessionEventHandlers().validationEnded(getAUTSessionId(), validationInfo.getValidationId(), validationResult);

        return result.getAsExpected();
    }
}
