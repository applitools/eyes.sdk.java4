package com.applitools.eyes.appium.ios;

import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class IOSCheckElementTest extends IOSTestSetup {

    @Test(groups = "failed") // App paths is absolute
    public void testIOSCheckElement() {
        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setSaveDebugScreenshots(false);

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        try {
            eyes.open(driver, "iOS test application", "Check element test");

            String xpath = "//XCUIElementTypeApplication[@name=\"FullpagescrolliOS\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeScrollView";
            eyes.check(Target.region(By.xpath(xpath)).fully().statusBarExists());

            eyes.close();
        } finally {
            eyes.abortIfNotClosed();
            driver.quit();
        }
    }

    @Override
    protected void setAppCapability() {
        capabilities.setCapability(MobileCapabilityType.APP, "/Users/alexanderkachechka/Downloads/FullpagescrolliOS.app");
    }
}
