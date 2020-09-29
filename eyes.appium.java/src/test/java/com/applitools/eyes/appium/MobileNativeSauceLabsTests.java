package com.applitools.eyes.appium;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.Region;
import com.applitools.eyes.fluent.ICheckSettings;
import com.applitools.eyes.selenium.fluent.Target;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;

public class MobileNativeSauceLabsTests {

    public final static String SAUCE_USERNAME = System.getenv("SAUCE_USERNAME");
    public final static String SAUCE_ACCESS_KEY = System.getenv("SAUCE_ACCESS_KEY");
    public final static String SAUCE_SELENIUM_URL = "https://ondemand.saucelabs.com/wd/hub";
    private Eyes eyes;

    @BeforeMethod
    public void SetupTest()
    {
        eyes = new Eyes();
        eyes.setBatch(new BatchInfo("Java4 Appium Mobile Native Tests"));
    }

    @Test
    public void AndroidNativeAppWindowTest() throws Exception {
        WebDriver driver = new AndroidDriver(new URL(SAUCE_SELENIUM_URL), getAndroidCapabilities());
        try {
            eyes.open(driver, "AndroidNativeApp", "AndroidNativeApp checkWindow");
            eyes.check("Contact list", com.applitools.eyes.selenium.fluent.Target.window().ignore(new Region(0,0,1440,100)));
            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }

    @Test
    public void AndroidNativeAppRegionTest() throws Exception {
        WebDriver driver = new AndroidDriver(new URL(SAUCE_SELENIUM_URL), getAndroidCapabilities());
        try {
            eyes.open(driver, "AndroidNativeApp", "AndroidNativeApp checkRegionFloating");
            ICheckSettings settings = com.applitools.eyes.selenium.fluent.Target.region(new Region(0, 100, 1400, 2000))
                    .floating(new Region(10, 10, 20, 20), 3, 3, 20, 30);
            eyes.check("Contact list - Region with floating region", settings);
            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }

    @Test
    public void iOSNativeAppTest() throws Exception {
        WebDriver driver = null;
        try {
            driver = new IOSDriver(new URL(SAUCE_SELENIUM_URL), getIOSCapabilities());
            eyes.open(driver, "iOSNativeApp", "iOSNativeApp checkWindow");
            eyes.check("checkWindow", com.applitools.eyes.selenium.fluent.Target.window().ignore(new Region(0,0,300,100)));
            eyes.close();
        } finally {
            if (driver != null) {
                driver.quit();
            }
            eyes.abortIfNotClosed();
        }
    }

    @Test
    public void iOSNativeAppRegionTest() throws Exception {
        WebDriver driver = null;
        try {
            driver = new IOSDriver(new URL(SAUCE_SELENIUM_URL), getIOSCapabilities());
            eyes.open(driver, "iOSNativeApp", "iOSNativeApp checkRegionFloating");
            ICheckSettings settings = Target.region(new Region(0, 100, 375, 712))
                    .floating(new Region(10, 10, 20, 20), 3, 3, 20, 30);
            eyes.check("Fluent - Window with floating region", settings);
            eyes.close();
        } finally {
            if (driver != null) {
                driver.quit();
            }
            eyes.abortIfNotClosed();
        }
    }

    private DesiredCapabilities getAndroidCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Samsung Galaxy S9 WQHD GoogleAPI Emulator");
        capabilities.setCapability("platformVersion", "8.1");
        capabilities.setCapability("app", "http://saucelabs.com/example_files/ContactManager.apk");
        capabilities.setCapability("clearSystemFiles", true);
        capabilities.setCapability("noReset", true);
        capabilities.setCapability("username", SAUCE_USERNAME);
        capabilities.setCapability("accesskey", SAUCE_ACCESS_KEY);
        return capabilities;
    }

    private DesiredCapabilities getIOSCapabilities() {
        DesiredCapabilities capabilities = DesiredCapabilities.iphone();
        capabilities.setCapability("deviceName","iPhone XS Simulator");
        capabilities.setCapability("platformVersion", "12.2");
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/HelloWorldiOS_1_0.zip");
        capabilities.setCapability("clearSystemFiles", true);
        capabilities.setCapability("noReset", true);
        capabilities.setCapability("NATIVE_APP", true);
        capabilities.setCapability("browserName", "");
        capabilities.setCapability("name", "iOSNativeApp checkWindow");
        capabilities.setCapability("username", SAUCE_USERNAME);
        capabilities.setCapability("accesskey", SAUCE_ACCESS_KEY);
        return capabilities;
    }
}