package com.applitools.eyes.appium;

import com.applitools.eyes.Location;
import com.applitools.eyes.Logger;
import com.applitools.eyes.positioning.PositionMemento;
import java.util.HashMap;
import org.openqa.selenium.WebElement;

public class IOSScrollPositionProvider extends AppiumScrollPositionProvider {

    private static final String SCROLL_DIRECTION_UP = "up";
    private static final String SCROLL_DIRECTION_DOWN = "down";
    private static final String SCROLL_DIRECTION_LEFT = "left";
    private static final String SCROLL_DIRECTION_RIGHT = "right";

    public IOSScrollPositionProvider (Logger logger, EyesAppiumDriver driver) {
        super(logger, driver);
    }

    /**
     * Go to the specified location.
     * @param location The position to scroll to.
     */
    public void setPosition(Location location) {
        logger.log("Warning: Appium cannot reliably scroll based on location; pass an element instead if you can. Doing nothing");
        Location curPos = getCurrentPosition();
        logger.verbose("Wanting to scroll to " + location);
        logger.verbose("Current scroll position is " + getCurrentPosition());
        Location lastPos = null;


        HashMap<String, String> args = new HashMap<>();
        String directionY = ""; // empty means we don't have to do any scrolling
        String directionX = "";
        if (curPos.getY() < location.getY()) {
            directionY = SCROLL_DIRECTION_DOWN;
        } else if (curPos.getY() > location.getY()) {
            directionY = SCROLL_DIRECTION_UP;
        }
        if (curPos.getX() < location.getX()) {
            directionX = SCROLL_DIRECTION_RIGHT;
        } else if (curPos.getX() > location.getX()) {
            directionX = SCROLL_DIRECTION_LEFT;
        }


        // first handle any vertical scrolling
        if (directionY != "") {
            logger.verbose("Scrolling to Y component");
            args.put("direction", directionY);
            while ((directionY == SCROLL_DIRECTION_DOWN && curPos.getY() < location.getY()) ||
                (directionY == SCROLL_DIRECTION_UP && curPos.getY() > location.getY())) {
                logger.verbose("Scrolling " + directionY);
                driver.executeScript("mobile: scroll", args);
                lastPos = curPos;
                curPos = getCurrentPosition();
                logger.verbose("Scrolled to " + curPos);
                if (curPos.getY() == lastPos.getY()) {
                    logger.verbose("Ended up at the same place as last scroll, stopping");
                    break;
                }
            }
        }

        // then handle any horizontal scrolling
        if (directionX != "") {
            logger.verbose("Scrolling to X component");
            args.put("direction", directionX);
            while ((directionX == SCROLL_DIRECTION_RIGHT && curPos.getX() < location.getX()) ||
                (directionX == SCROLL_DIRECTION_LEFT && curPos.getX() > location.getX())) {
                logger.verbose("Scrolling " + directionY);
                driver.executeScript("mobile: scroll", args);
                lastPos = curPos;
                curPos = getCurrentPosition();
                if (curPos.getX() == lastPos.getX()) {
                    logger.verbose("Ended up at the same place as last scroll, stopping");
                    break;
                }
            }
        }
    }

    public void setPosition(WebElement element) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("toVisible", "true");
        params.put("element", element);
        driver.executeScript("mobile: scroll", params);
    }

    public void restoreState(PositionMemento state) {
        logger.log("Warning: AppiumScrollPositionProvider cannot reliably restore position state; doing nothing");
    }


    private double getScrollDistanceRatio() {
        if (distanceRatio == 0.0) {
            int viewportHeight = eyesDriver.getDefaultContentViewportSize(false).getHeight() + eyesDriver.getStatusBarHeight();
            double pixelRatio = eyesDriver.getDevicePixelRatio();
            // viewport height is in device pixels, whereas element heights are in logical pixels,
            // so need to scale the scrollview height accordingly.
            // FIXME: 29/11/2018 should the scrollviewHeight be indeed UNSCALED and WITHOUT the scrollgap
            //double scrollviewHeight = ((getScrollableViewRegion().getHeight() - verticalScrollGap) * pixelRatio);
            double scrollviewHeight = getScrollableViewRegion().getHeight();
            distanceRatio = scrollviewHeight / viewportHeight;
            logger.verbose("Distance ratio for scroll down based on viewportHeight of " + viewportHeight +
                " and scrollview height of " + scrollviewHeight + " is " + Double.toString(distanceRatio));
        }

        return distanceRatio;
    }

    public Location scrollDown(boolean returnAbsoluteLocation) {
        EyesAppiumUtils.scrollByDirection(driver, SCROLL_DIRECTION_DOWN, getScrollDistanceRatio());
        return getCurrentPositionWithoutStatusBar(returnAbsoluteLocation);
    }

    @Override
    public void scrollTo(int startX, int startY, int endX, int endY) {
        // Do not need this method
    }

}
