package com.applitools.eyes.appium.ios;

import com.applitools.eyes.appium.TestSetup;
import io.appium.java_client.ios.IOSDriver;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class IOSTestSetup extends TestSetup {

    @Override
    protected void setCapabilities() {
        super.setCapabilities();
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("deviceName", "iPhone 8");
        capabilities.setCapability("os_version", "13.3");
        capabilities.setCapability("automationName", "XCUITest");
        capabilities.setCapability("newCommandTimeout", 300);
        setResetMode();
    }

    @Override
    protected void initDriver() throws MalformedURLException {
        driver = new IOSDriver<>(new URL(appiumServerUrl), capabilities);
    }

    @Override
    protected void setAppCapability() {
        // To run locally use https://applitools.bintray.com/Examples/ipa/IOSTestApp.ipa
        capabilities.setCapability("app", "app_ios");
    }

    protected void setResetMode() {
        capabilities.setCapability("fullReset", true);
    }
}
