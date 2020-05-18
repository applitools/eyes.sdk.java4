package com.applitools.eyes.selenium;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.TestUtils;
import com.applitools.eyes.selenium.fluent.Target;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class AndroidTest {

    private static BatchInfo batchInfo = new BatchInfo("Java3 Tests");

    @BeforeClass
    public static void classSetup() {
        String batchId = System.getenv("APPLITOOLS_BATCH_ID");
        if (batchId != null) {
            batchInfo.setId(batchId);
        }
    }

    @DataProvider(parallel = true)
    public static Object[][] data() {
        Object[][] googlePixelPermutations = TestUtils.generatePermutations(
                Arrays.asList(new Object[]{"Google Pixel GoogleAPI Emulator"}), // device
                Arrays.asList(new Object[]{"portrait", "landscape"}), // orientation
                Arrays.asList(new Object[]{"7.1"}), // OS Version
                Arrays.asList(new Object[]{false, true}) // fully
        );

        ArrayList<Object[]> returnValue = new ArrayList<>();
        returnValue.addAll(Arrays.asList(googlePixelPermutations));

        return returnValue.toArray(new Object[0][]);
    }

    @Test(dataProvider = "data")
    public void TestAndroidChromeCrop(String deviceName, String deviceOrientation, String platformVersion, boolean fully) throws MalformedURLException {
        Eyes eyes = new Eyes();

        eyes.setBatch(batchInfo);

        // This is your api key, make sure you use it in all your tests.
        DesiredCapabilities caps = DesiredCapabilities.iphone();
        caps.setCapability("appiumVersion", "1.7.2");
        caps.setCapability("deviceName", deviceName);
        caps.setCapability("deviceOrientation", deviceOrientation);
        caps.setCapability("platformVersion", platformVersion);
        caps.setCapability("platformName", "Android");
        caps.setCapability("browserName", "Chrome");

        caps.setCapability("username", System.getenv("SAUCE_USERNAME"));
        caps.setCapability("accesskey", System.getenv("SAUCE_ACCESS_KEY"));

        String sauceUrl = "http://ondemand.saucelabs.com/wd/hub";
        WebDriver driver = new RemoteWebDriver(new URL(sauceUrl), caps);
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        eyes.setLogHandler(new StdoutLogHandler(true));

        String testName = String.format("%s %s %s", deviceName, platformVersion, deviceOrientation);
        if (fully) {
            testName += " fully";
        }

        if (System.getenv("CI") == null) {
            //String logFilename = String.format("c:\\temp\\logs\\iostest_%s.log", testName);
            //eyes.setLogHandler(new FileLogger(logFilename, false, true));
            //eyes.setImageCut(new FixedCutProvider(30, 12, 8, 5));
            //eyes.setForceFullPageScreenshot(true);
            //eyes.setSaveDebugScreenshots(true);
            //eyes.setDebugScreenshotsPath("C:\\temp\\logs");
            //eyes.setDebugScreenshotsPrefix("iostest_" + testName);
        } else {
            eyes.setLogHandler(new StdoutLogHandler(true));
        }

        eyes.setStitchMode(StitchMode.SCROLL);

        eyes.addProperty("Orientation", deviceOrientation);
        eyes.addProperty("Stitched", fully ? "True" : "False");

        try {
            driver.get("https://www.applitools.com/customers");
            eyes.open(driver, "Eyes Selenium SDK - Android Chrome Cropping", testName);
            eyes.check("Initial view", Target.region(By.cssSelector("body")).fully(fully));
            eyes.close();
        } finally {
            eyes.abortIfNotClosed();
            driver.quit();
        }
    }
}