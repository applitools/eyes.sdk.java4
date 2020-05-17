package com.applitools.eyes.appium;

import com.applitools.eyes.*;
import com.applitools.eyes.metadata.ImageMatchSettings;
import com.applitools.eyes.metadata.SessionResults;
import com.applitools.eyes.selenium.config.Configuration;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class AndroidAccessibilityTest {

    @Test
    public void testAndroidAccessibility() throws Exception {
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

        Configuration configuration = new Configuration();
        configuration.setAppName("Android Test App");
        configuration.setTestName("Test accessibility");
        configuration.setAccessibilityValidation(new AccessibilitySettings(AccessibilityLevel.AA, AccessibilityGuidelinesVersion.WCAG_2_0));

        try {
            eyes.open(driver, configuration);
            eyes.check("Launcher screen", Target.window().accessibility(By.xpath("//*[@text='ScrollView']"), AccessibilityRegionType.GraphicalObject));
            TestResults results = eyes.close();

            SessionAccessibilityStatus accessibilityStatus = results.getAccessibilityStatus();
            Assert.assertEquals(accessibilityStatus.getLevel(), AccessibilityLevel.AA);
            Assert.assertEquals(accessibilityStatus.getVersion(), AccessibilityGuidelinesVersion.WCAG_2_0);

            SessionResults sessionResults = TestUtils.getSessionResults(eyes.getApiKey(), results);
            com.applitools.eyes.metadata.ImageMatchSettings defaultMatchSettings = sessionResults.getStartInfo().getDefaultMatchSettings();
            Assert.assertEquals(defaultMatchSettings.getAccessibilitySettings().getGuidelinesVersion(), AccessibilityGuidelinesVersion.WCAG_2_0);
            Assert.assertEquals(defaultMatchSettings.getAccessibilitySettings().getLevel(), AccessibilityLevel.AA);

            ImageMatchSettings matchSettings = sessionResults.getActualAppOutput()[0].getImageMatchSettings();
            List<AccessibilityRegionByRectangle> actual = Arrays.asList(matchSettings.getAccessibility());
            Assert.assertEquals(new HashSet<>(actual), new HashSet<>(Collections.singletonList(
                    new AccessibilityRegionByRectangle(15, 358, 382, 48, AccessibilityRegionType.GraphicalObject)
            )));

            configuration.setAccessibilityValidation(null);
            eyes.open(driver, configuration);
            eyes.checkWindow("No accessibility");
            results = eyes.close();
            Assert.assertNull(results.getAccessibilityStatus());
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }
}
