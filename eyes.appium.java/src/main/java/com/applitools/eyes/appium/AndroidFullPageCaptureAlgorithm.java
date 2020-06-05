package com.applitools.eyes.appium;

import com.applitools.eyes.*;
import com.applitools.eyes.capture.EyesScreenshotFactory;
import com.applitools.eyes.capture.ImageProvider;
import com.applitools.eyes.debug.DebugScreenshotsProvider;
import com.applitools.eyes.selenium.positioning.NullRegionPositionCompensation;
import com.applitools.utils.GeneralUtils;
import com.google.common.math.IntMath;

import java.awt.image.BufferedImage;
import java.math.RoundingMode;

public class AndroidFullPageCaptureAlgorithm extends AppiumFullPageCaptureAlgorithm {

    private static final int DEFAULT_STITCHING_ADJUSTMENT = 50;

    private Integer stitchingAdjustment = DEFAULT_STITCHING_ADJUSTMENT;

    public AndroidFullPageCaptureAlgorithm(Logger logger,
        AppiumScrollPositionProvider scrollProvider,
        ImageProvider imageProvider, DebugScreenshotsProvider debugScreenshotsProvider,
        ScaleProviderFactory scaleProviderFactory, CutProvider cutProvider,
        EyesScreenshotFactory screenshotFactory, int waitBeforeScreenshots, Integer stitchingAdjustment) {

        super(logger, scrollProvider, imageProvider, debugScreenshotsProvider,
            scaleProviderFactory, cutProvider, screenshotFactory, waitBeforeScreenshots, null);

        // Android returns pixel coordinates which are already scaled according to the pixel ratio
        this.coordinatesAreScaled = true;
        if (stitchingAdjustment != null) {
            this.stitchingAdjustment = stitchingAdjustment;
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void captureAndStitchTailParts(BufferedImage image, int stitchingOverlap, RectangleSize entireSize, RectangleSize initialPartSize) {
        // scrollViewRegion is the (upscaled) region of the scrollview on the screen
        Region scrollViewRegion = scaleSafe(((AppiumScrollPositionProvider) scrollProvider).getScrollableViewRegion());
        // we modify the region by one pixel to make sure we don't accidentally get a pixel of the header above it
        Location newLoc = new Location(scrollViewRegion.getLeft(), scrollViewRegion.getTop() - scaleSafe(((AppiumScrollPositionProvider) scrollProvider).getStatusBarHeight()));
        RectangleSize newSize = new RectangleSize(initialPartSize.getWidth(), scrollViewRegion.getHeight());
        scrollViewRegion.setLocation(newLoc);
        scrollViewRegion.setSize(newSize);

        ContentSize contentSize = ((AppiumScrollPositionProvider) scrollProvider).getCachedContentSize();

        int xPos = scrollViewRegion.getLeft() + 1;
        Region regionToCrop;

        // We need to set position margin to avoid shadow at the top of view
        int oneScrollStep = scrollViewRegion.getHeight() - stitchingAdjustment;
        int maxScrollSteps = contentSize.getScrollContentHeight() / oneScrollStep;
        logger.verbose("Entire scrollable height: " + contentSize.getScrollContentHeight());
        logger.verbose("One scroll step: " + oneScrollStep);
        logger.verbose("Max scroll steps: " + ((double) contentSize.getScrollContentHeight() / oneScrollStep));

        for (int step = 1; step <= maxScrollSteps; step++) {
            regionToCrop = new Region(0,
                    scrollViewRegion.getTop() + stitchingAdjustment,
                    initialPartSize.getWidth(),
                    scrollViewRegion.getHeight() - stitchingAdjustment);

            currentPosition = new Location(0,
                    scrollViewRegion.getTop() + ((scrollViewRegion.getHeight()) * (step)) - (stitchingAdjustment*step - stitchingAdjustment));

            // We should use release() touch action for the last scroll action
            // For some applications scrolling is not executed for the last part with cancel() action
            ((AppiumScrollPositionProvider) scrollProvider).scrollTo(xPos,
                    scrollViewRegion.getHeight() + scrollViewRegion.getTop() - 1,
                    xPos,
                    scrollViewRegion.getTop() + (step != maxScrollSteps ? stitchingAdjustment : 0),
                    step != maxScrollSteps);

            if (step == maxScrollSteps) {
                int cropTo = contentSize.getScrollContentHeight() - (oneScrollStep * (step));
                int cropFrom = oneScrollStep - cropTo + scrollViewRegion.getTop() + stitchingAdjustment;
                regionToCrop = new Region(0,
                        cropFrom,
                        initialPartSize.getWidth(),
                        cropTo);
                currentPosition = new Location(0,
                        scrollViewRegion.getTop() + ((scrollViewRegion.getHeight()) * (step)) - (stitchingAdjustment*step));
            }
            captureAndStitchCurrentPart(regionToCrop, scrollViewRegion);
        }

        int heightUnderScrollableView = initialPartSize.getHeight() - oneScrollStep - scrollViewRegion.getTop();
        if (heightUnderScrollableView > 0) { // check if there is views under the scrollable view
            regionToCrop = new Region(0, scrollViewRegion.getHeight() + scrollViewRegion.getTop() - stitchingAdjustment, initialPartSize.getWidth(), heightUnderScrollableView);

            currentPosition = new Location(0, scrollViewRegion.getTop() + contentSize.getScrollContentHeight() - stitchingAdjustment);

            captureAndStitchCurrentPart(regionToCrop, scrollViewRegion);
        }

        moveToTopLeft();
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected RectangleSize captureAndStitchCurrentPart(Region partRegion, Region scrollViewRegion) {
        logger.verbose("Taking screenshot for current scroll location");
        GeneralUtils.sleep(waitBeforeScreenshots);
        BufferedImage partImage = imageProvider.getImage();
        debugScreenshotsProvider.save(partImage,
                "original-scrolled=" + currentPosition.toStringForFilename());

        // before we take new screenshots, we have to reset the region in the screenshot we care
        // about, since from now on we just want the scroll view, not the entire view
        setRegionInScreenshot(partImage, partRegion, new NullRegionPositionCompensation());

        partImage = cropPartToRegion(partImage, partRegion);

        stitchPartIntoContainer(partImage);
        return new RectangleSize(partImage.getWidth(), partImage.getHeight());
    }
}
