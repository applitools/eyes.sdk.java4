package com.applitools.eyes.appium.ios;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.StdoutLogHandler;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

public class AppiumIOSFullPageScreenshotTest extends IOSTestSetup {

    @Test(groups = "failed") // App paths is absolute
    public void testAppiumIOSFullPageScreenshot() {
        LogHandler logHandler = new StdoutLogHandler(true);
        eyes.setLogHandler(logHandler);
//        eyes.setForceFullPageScreenshot(true);
//        eyes.setSaveDebugScreenshots(true);
//        eyes.setDebugScreenshotsPath("/Users/jlipps/Desktop");
        eyes.setScrollToRegion(false);
        eyes.setMatchTimeout(1000);
        eyes.setStitchOverlap(44);

        try {

            // Start the test.
            eyes.open(driver, "Applitools Demo App", "Appium Native iOS with Full page screenshot");
            WebElement showTable = driver.findElement(MobileBy.AccessibilityId("Show table view"));
            eyes.checkRegion(showTable);
            showTable.click();
            eyes.checkWindow("Big Table");
//            driver.findElement(MobileBy.AccessibilityId("Collection view")).click();
//            eyes.checkWindow("Short collection view");

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
        // TODO do not merge until this is replaced with a non-user-specific path
        capabilities.setCapability("app", "/Users/danielputerman/test/sdk/appium/xcui-test-app/eyesxcui-demo-app.app");

        // TODO move to other method
        capabilities.setCapability("useNewWDA", false);
        capabilities.setCapability("noReset", true);
        capabilities.setCapability("newCommandTimeout", 300);
    }
}
