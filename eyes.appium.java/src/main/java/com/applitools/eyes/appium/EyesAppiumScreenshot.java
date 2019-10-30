package com.applitools.eyes.appium;

import com.applitools.eyes.*;
import com.applitools.eyes.selenium.capture.EyesWebDriverScreenshot;
import com.applitools.utils.ImageUtils;

import java.awt.image.BufferedImage;

public class EyesAppiumScreenshot extends EyesWebDriverScreenshot {

    private final EyesAppiumDriver driver;

    public EyesAppiumScreenshot(Logger logger, EyesAppiumDriver driver, BufferedImage image) {
        super(logger, driver, image);
        this.driver = driver;
    }

    public EyesAppiumScreenshot(Logger logger, EyesAppiumDriver driver, BufferedImage image, Region screenshotRegion) {
        super(logger, driver, image, screenshotRegion);
        this.driver = driver;
    }

    public EyesAppiumScreenshot(Logger logger, EyesAppiumDriver driver, BufferedImage image, RectangleSize entireFrameSize) {
        super(logger, driver, image, entireFrameSize);
        this.driver = driver;
    }

    @Override
    public Location getLocationInScreenshot(Location location, CoordinatesType coordinatesType) throws OutOfBoundsException {
        return location;
    }

    @Override
    public Location getLocationInScreenshot(Location location, Location originalLocation, CoordinatesType coordinatesType) throws OutOfBoundsException {
        if (location.getY() < originalLocation.getY()) {
            return location;
        } else {
            return new Location(location.getX(), location.getY() - driver.getStatusBarHeight());
        }
    }

    @Override
    public EyesAppiumScreenshot getSubScreenshot(Region region, boolean throwIfClipped) {
        BufferedImage subImage = ImageUtils.getImagePart(image, region);
        return new EyesAppiumScreenshot(logger, driver, subImage,
                new RectangleSize(subImage.getWidth(), subImage.getHeight()));
    }

    @Override
    public EyesWebDriverScreenshot getSubScreenshotForRegion(Region region, boolean throwIfClipped) {
        return super.getSubScreenshotForRegion(region, throwIfClipped);
    }

    @Override
    public Location convertLocation(Location location, CoordinatesType from, CoordinatesType to) {
        return location;
    }
}
