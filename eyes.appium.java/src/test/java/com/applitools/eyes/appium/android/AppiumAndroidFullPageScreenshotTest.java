package com.applitools.eyes.appium.android;

import com.applitools.eyes.StdoutLogHandler;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

public class AppiumAndroidFullPageScreenshotTest extends AndroidTestSetup {

    @Test
    public void testAppiumAndroidFullPageScreenshot() {
        driver.manage().timeouts().implicitlyWait(5000, TimeUnit.MILLISECONDS);

        eyes.setForceFullPageScreenshot(true);
        eyes.setMatchTimeout(1000);
//        eyes.setProxy(new ProxySettings("http://localhost:8888"));

        // Start the test.
        eyes.open(driver, "Applitools Demo", "Appium Native Android with Full Page Screenshot");

        eyes.checkWindow("scroll");

        // End the test.
        eyes.close();
    }
}
