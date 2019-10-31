package com.applitools.eyes.appium;

import com.applitools.eyes.Location;
import com.applitools.eyes.Region;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.locators.VisualLocator;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AndroidVisualLocatorsTest {

    public static void main(String[] args) throws Exception {

        DesiredCapabilities capabilities = new DesiredCapabilities();
        // IMPORTANT: This assumes an emulator running locally. Make sure the settings match the device you're running.
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Google Nexus 6");
        capabilities.setCapability("platformVersion", "7.1.1");

        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/app-android.apk");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("newCommandTimeout", 300);

        AndroidDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        driver.manage().timeouts().implicitlyWait(10_000, TimeUnit.MILLISECONDS);

        Eyes eyes = new Eyes();

        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setForceFullPageScreenshot(false);
        eyes.setMatchTimeout(1000);
        eyes.setSaveDebugScreenshots(false);

        try {
            eyes.open(driver, "Android Test App", "Test Visual Locators");

            eyes.checkWindow("Launch screen test");

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

            locators = eyes.locate(VisualLocator.name("header_locator"));
            System.out.println("Received locators" + locators);

            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }
}
