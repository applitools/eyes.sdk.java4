package com.applitools.eyes.appium.ios;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

public class ScrollViewTest extends IOSTestSetup {

    @Test(groups = "failed") // DiffsFoundException
    public void testScrollView() {
        LogHandler logHandler = new StdoutLogHandler(true);
        eyes.setLogHandler(logHandler);
        eyes.setMatchTimeout(1000);

        try {
            // Start the test.
            eyes.open(driver, "IOS test application", "XCUIElementTypeScrollView");

            WebElement showScrollView = driver.findElement(MobileBy.AccessibilityId("Scroll view"));
            showScrollView.click();

            // Check viewport.
            eyes.check(Target.window().fully(false).withName("Window Viewport"));
            // Full page screenshot.
            eyes.check(Target.window().fully(true).withName("Window Fullpage"));
            // Check scroll view's viewport.
            eyes.check(Target.region(MobileBy.xpath("//XCUIElementTypeScrollView[1]")).withName("ScrollView Viewport"));
            // Check full content of scroll view.
            eyes.check(Target.region(MobileBy.xpath("//XCUIElementTypeScrollView[1]")).fully().withName("ScrollView Fullpage"));

            // End the test.
            eyes.close();

        } finally {

            // Close the app.
            driver.quit();

            // If the test was aborted before eyes.close was called, ends the test as aborted.
            eyes.abortIfNotClosed();

        }
    }

    @Override
    protected void setAppCapability() {
        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/IOSTestApp/1.1/IOSTestApp-1.1.zip");
    }
}
