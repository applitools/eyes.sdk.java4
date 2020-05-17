package com.applitools.eyes.appium;

import com.applitools.eyes.*;
import com.applitools.eyes.selenium.config.Configuration;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URL;

public class AndroidAccessibilityTest {

    @Test
    public void testSanity() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Google Nexus 6");

        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/app-android.apk");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("newCommandTimeout", 300);

        AndroidDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        Eyes eyes = new Eyes();
        eyes.setForceFullPageScreenshot(false);
        eyes.setMatchTimeout(1000);
        eyes.setSaveDebugScreenshots(false);
        eyes.setProxy(new ProxySettings("http://localhost:8888"));
        eyes.setServerUrl("https://testeyesapi.applitools.com");
        eyes.setApiKey("D98LyaCRbaPoEDpIyF99AKiUHAzx1JUoqITFiyF104mHniE110");

        Configuration configuration = new Configuration();
        configuration.setAppName("Android Test App");
        configuration.setTestName("Test accessibility");
        configuration.setAccessibilityValidation(new AccessibilitySettings(AccessibilityLevel.AA, AccessibilityGuidelinesVersion.WCAG_2_0));

        try {
            eyes.open(driver, configuration);

            eyes.check("Launcher screen", Target.window().accessibility(By.xpath("//*[@text='ScrollView']"), AccessibilityRegionType.GraphicalObject));
            eyes.check("Launcher screen", Target.window().accessibility(By.xpath("//*[@text='ListView']"), AccessibilityRegionType.GraphicalObject));

            TestResults results = eyes.close();
            SessionAccessibilityStatus accessibilityStatus = results.getAccessibilityStatus();
            Assert.assertEquals(accessibilityStatus.getSettings().getLevel(), AccessibilityLevel.AA);
            Assert.assertEquals(accessibilityStatus.getSettings().getGuidelinesVersion(), AccessibilityGuidelinesVersion.WCAG_2_0);
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }
}
