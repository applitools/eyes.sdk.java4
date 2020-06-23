package com.applitools.eyes.appium.android;

import com.applitools.eyes.StdoutLogHandler;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

public class AppiumAndroidFullPageScreenshotTest extends AndroidTestSetup {

    @Test(groups = "failed") // App paths is absolute
    public void testAppiumAndroidFullPageScreenshot() {
        driver.manage().timeouts().implicitlyWait(5000, TimeUnit.MILLISECONDS);

        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setForceFullPageScreenshot(true);
        eyes.setMatchTimeout(1000);
//        eyes.setProxy(new ProxySettings("http://localhost:8888"));

        try {
            // Start the test.
            eyes.open(driver, "Applitools Demo", "Appium Native Android with Full Page Screenshot");

            eyes.checkWindow("scroll");

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
        capabilities.setCapability("app", "/Users/danielputerman/devel/applitools/appium-test-apps/android-appium-demo/app-debug.apk");
    }
}
