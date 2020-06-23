package com.applitools.eyes.appium.ios;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

public class TableViewTest extends IOSTestSetup {

    @Test(groups = "failed") // DiffsFoundException
    public void testTableView() {
        LogHandler logHandler = new StdoutLogHandler(true);
        eyes.setLogHandler(logHandler);
        eyes.setMatchTimeout(1000);

        try {
            // Start the test.
            eyes.open(driver, "IOS test application", "XCUIElementTypeTable");

            WebElement showTable = driver.findElement(MobileBy.AccessibilityId("Table view"));
            showTable.click();

            // Check viewport.
            eyes.check(Target.window().fully(false).withName("Window Viewport"));
            // Full page screenshot.
            eyes.check(Target.window().fully(true).withName("Window Fullpage"));
            // Check table view's viewport.
            eyes.check(Target.region(MobileBy.xpath("//XCUIElementTypeTable[1]")).withName("Table Viewport"));
            // Check full content of table view.
            eyes.check(Target.region(MobileBy.xpath("//XCUIElementTypeTable[1]")).fully().withName("Table Fullpage"));

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
