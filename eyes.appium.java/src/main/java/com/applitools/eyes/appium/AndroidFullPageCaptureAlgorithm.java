package com.applitools.eyes.appium;

import com.applitools.eyes.*;
import com.applitools.eyes.capture.EyesScreenshotFactory;
import com.applitools.eyes.capture.ImageProvider;
import com.applitools.eyes.debug.DebugScreenshotsProvider;
import com.applitools.eyes.selenium.positioning.NullRegionPositionCompensation;
import com.applitools.utils.GeneralUtils;

import java.awt.image.BufferedImage;

public class AndroidFullPageCaptureAlgorithm extends AppiumFullPageCaptureAlgorithm {

    public AndroidFullPageCaptureAlgorithm(Logger logger,
        AppiumScrollPositionProvider scrollProvider,
        ImageProvider imageProvider, DebugScreenshotsProvider debugScreenshotsProvider,
        ScaleProviderFactory scaleProviderFactory, CutProvider cutProvider,
        EyesScreenshotFactory screenshotFactory, int waitBeforeScreenshots) {

        super(logger, scrollProvider, imageProvider, debugScreenshotsProvider,
            scaleProviderFactory, cutProvider, screenshotFactory, waitBeforeScreenshots, null);

        // Android returns pixel coordinates which are already scaled according to the pixel ratio
        this.coordinatesAreScaled = true;
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void captureAndStitchTailParts(BufferedImage image, int stitchingOverlap, RectangleSize entireSize, RectangleSize initialPartSize) {
        // scrollViewRegion is the (upscaled) region of the scrollview on the screen
        Region scrollViewRegion = scaleSafe(((AppiumScrollPositionProvider) scrollProvider).getScrollableViewRegion());
        // we modify the region by one pixel to make sure we don't accidentally get a pixel of the header above it
        Location newLoc = new Location(0, scrollViewRegion.getTop() - scaleSafe(((AppiumScrollPositionProvider) scrollProvider).getStatusBarHeight()));
        RectangleSize newSize = new RectangleSize(initialPartSize.getWidth(), scrollViewRegion.getHeight());
        scrollViewRegion.setLocation(newLoc);
        scrollViewRegion.setSize(newSize);

        ContentSize contentSize = ((AppiumScrollPositionProvider) scrollProvider).getCachedContentSize();

        int positionMargin = 50; // We need to set position margin to avoid shadow at the top of view

        int xPos = entireSize.getWidth()/2;
        Region regionToCrop;
        int oneScrollStep = scrollViewRegion.getHeight() - positionMargin;
        int maxScrollSteps = (contentSize.scrollableOffset + contentSize.height) / oneScrollStep;

        for (int step = 1; step <= maxScrollSteps; step++) {
            regionToCrop = new Region(0,
                    scrollViewRegion.getTop() + positionMargin,
                    initialPartSize.getWidth(),
                    scrollViewRegion.getHeight() - positionMargin);

            currentPosition = new Location(0,
                    scrollViewRegion.getTop() + ((scrollViewRegion.getHeight()) * (step)) - (positionMargin*step - positionMargin));

            ((AppiumScrollPositionProvider) scrollProvider).scrollTo(xPos,
                    scrollViewRegion.getHeight() + scrollViewRegion.getTop() - 1,
                    xPos,
                    scrollViewRegion.getTop() + (step != maxScrollSteps ? positionMargin : 0));

            if (step == maxScrollSteps) {
                int cropTo = (contentSize.scrollableOffset + contentSize.height) - (oneScrollStep * (step));
                int cropFrom = oneScrollStep - cropTo + scrollViewRegion.getTop() + positionMargin;
                regionToCrop = new Region(0,
                        cropFrom,
                        initialPartSize.getWidth(),
                        cropTo);
                currentPosition = new Location(0,
                        scrollViewRegion.getTop() + ((scrollViewRegion.getHeight()) * (step)) - (positionMargin*step));
            }
            captureAndStitchCurrentPart(regionToCrop, scrollViewRegion);
        }

        int heightUnderScrollableView = initialPartSize.getHeight() - oneScrollStep - scrollViewRegion.getTop();
        if (heightUnderScrollableView > 0) { // check if there is views under the scrollable view
            regionToCrop = new Region(0, scrollViewRegion.getHeight() + scrollViewRegion.getTop() - positionMargin, initialPartSize.getWidth(), heightUnderScrollableView);

            currentPosition = new Location(0, scrollViewRegion.getTop() + (contentSize.scrollableOffset + contentSize.height) - positionMargin);

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
