package com.applitools.eyes.appium.ios;

import com.applitools.eyes.Location;
import com.applitools.eyes.LogHandler;
import com.applitools.eyes.Region;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.locators.VisualLocator;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IOSVisualLocatorTest extends IOSTestSetup {

    @Test(groups = "failed") // App paths is absolute
    public void testIOSVisualLocator() throws Exception {
        LogHandler logHandler = new StdoutLogHandler(true);
        eyes.setLogHandler(logHandler);
        eyes.setForceFullPageScreenshot(false);
        eyes.setScrollToRegion(false);
        eyes.setMatchTimeout(1000);
        try {
            eyes.open(driver, "iOS Test App", "Test Visual Locators");

            eyes.checkWindow("Launch screen");

            Map<String, List<Region>> locators = eyes.locate(VisualLocator.name("list_view_locator").name("scroll_view_locator"));
            System.out.println("Received locators" + locators);

            List<String> names = new ArrayList<>();
            names.add("list_view_locator");
            names.add("scroll_view_locator");
            locators = eyes.locate(VisualLocator.names(names));
            System.out.println("Received locators" + locators);


            locators = eyes.locate(VisualLocator.name("list_view_locator"));
            List<Region> listViewLocatorRegions = locators.get("list_view_locator");

            if (listViewLocatorRegions != null && !listViewLocatorRegions.isEmpty()) {
                Region listViewLocator = listViewLocatorRegions.get(0);
                Location clickLocation = new Location(listViewLocator.getLeft() + listViewLocator.getWidth() / 2,
                        listViewLocator.getTop() + listViewLocator.getHeight() / 2);

                TouchAction actionPress = new TouchAction(driver);
                actionPress.press(PointOption.point(clickLocation.getX(), clickLocation.getY())).waitAction(WaitOptions.waitOptions(Duration.ofMillis(500)));
                actionPress.release();
                driver.performTouchAction(actionPress);

                eyes.checkWindow("ListView screen");
            }
            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }

    @Override
    protected void setAppCapability() {
        capabilities.setCapability("app", "/Users/alexanderkachechka/Downloads/app-ios.app");
    }
}
