package com.applitools.eyes.appium;

import com.applitools.eyes.*;
import com.applitools.eyes.selenium.config.Configuration;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

public class AndroidAccessibilityTest {

    public static void main(String[] args) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Google Nexus 6");
        capabilities.setCapability("platformVersion", "7.1.1");

        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/app-android.apk");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("newCommandTimeout", 300);

        AndroidDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        Eyes eyes = new Eyes();

        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setForceFullPageScreenshot(false);
        eyes.setMatchTimeout(1000);
        eyes.setSaveDebugScreenshots(false);

        Configuration configuration = new Configuration();
        configuration.setAppName("Android Test App");
        configuration.setTestName("Test accessibility");
        configuration.setAccessibilityValidation(AccessibilityLevel.AAA);

        try {
            eyes.open(driver, configuration);

            eyes.check("Launcher screen", Target.window().accessibility(By.xpath("//*[@text='ScrollView']"), AccessibilityRegionType.GraphicalObject));
            eyes.check("Launcher screen", Target.window().accessibility(By.xpath("//*[@text='ListView']"), AccessibilityRegionType.GraphicalObject));

            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }
}
