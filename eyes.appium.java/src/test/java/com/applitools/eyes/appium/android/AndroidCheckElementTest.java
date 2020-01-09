package com.applitools.eyes.appium.android;

import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class AndroidCheckElementTest {

    public static void main(String[] args) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Google Nexus 6");
        capabilities.setCapability("platformVersion", "7.1.1");

        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/android_test_app.apk");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("newCommandTimeout", 300);

        AndroidDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        driver.manage().timeouts().implicitlyWait(10_000, TimeUnit.MILLISECONDS);

        Eyes eyes = new Eyes();

        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setMatchTimeout(1000);

        try {
            eyes.open(driver, "AndroidTestApp", "Check element test");

            eyes.check(Target.region(MobileBy.id("btn_recycler_view")));

            driver.findElementById("btn_recycler_view").click();

            eyes.check(Target.region(MobileBy.id("recycler_view")));

            eyes.check(Target.region(MobileBy.id("recycler_view")).fully());

            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }
}
