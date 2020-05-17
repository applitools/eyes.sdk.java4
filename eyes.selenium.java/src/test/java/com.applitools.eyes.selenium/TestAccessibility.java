package com.applitools.eyes.selenium;

import com.applitools.eyes.*;
import com.applitools.eyes.metadata.ImageMatchSettings;
import com.applitools.eyes.metadata.SessionResults;
import com.applitools.eyes.selenium.config.Configuration;
import com.applitools.eyes.selenium.fluent.Target;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TestAccessibility {

    @Test
    public void testAccessibility() throws Exception {
        Eyes eyes = new Eyes();
        AccessibilitySettings settings = new AccessibilitySettings(AccessibilityLevel.AA, AccessibilityGuidelinesVersion.WCAG_2_0);
        Configuration configuration = new Configuration();
        configuration.setAccessibilityValidation(settings);
        eyes.setProxy(new ProxySettings("http://localhost:8888"));
//        eyes.setServerUrl("https://testeyesapi.applitools.com");
//        eyes.setApiKey("D98LyaCRbaPoEDpIyF99AKiUHAzx1JUoqITFiyF104mHniE110");
        WebDriver driver = new ChromeDriver();
        try {
            driver.get("https://applitools.github.io/demo/TestPages/FramesTestPage/");
            eyes.open(driver);
            eyes.check("Sanity", Target.window().accessibility(By.className("ignore"), AccessibilityRegionType.LargeText));
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
            Assert.assertEquals(new HashSet<>(actual), new HashSet<>(Arrays.asList(
                    new AccessibilityRegionByRectangle(122, 928, 456, 306, AccessibilityRegionType.LargeText),
                    new AccessibilityRegionByRectangle(8, 1270, 690, 206, AccessibilityRegionType.LargeText),
                    new AccessibilityRegionByRectangle(10, 284, 800, 500, AccessibilityRegionType.LargeText)
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
