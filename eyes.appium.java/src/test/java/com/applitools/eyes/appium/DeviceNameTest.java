package com.applitools.eyes.appium;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.StdoutLogHandler;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

public class DeviceNameTest {

    public static void main(String[] args) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Google Nexus 6");
        capabilities.setCapability("platformVersion", "7.1.1");

        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/app-android.apk");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("newCommandTimeout", 300);

        AndroidDriver driver = new AndroidDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        Eyes eyes = new Eyes();
        eyes.setApiKey(System.getenv("APPLITOOLS_API_KEY"));

        LogHandler logHandler = new StdoutLogHandler(true);
        eyes.setLogHandler(logHandler);
        eyes.setScrollToRegion(false);

        try {
            eyes.open(driver, "Applitools Android Test App", "Test device name");
            eyes.checkWindow();
            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }
}
