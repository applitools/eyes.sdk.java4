package com.applitools.eyes.appium.ios;

import com.applitools.eyes.appium.TestSetup;
import io.appium.java_client.ios.IOSDriver;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class IOSTestSetup extends TestSetup {

    @Override
    public void setCapabilities() {
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("deviceName", "iPhone 8");
        capabilities.setCapability("platformVersion", "13.3");

        capabilities.setCapability("automationName", "XCUITest");
        capabilities.setCapability("fullReset", true);
        capabilities.setCapability("newCommandTimeout", 300);

        setAppCapability();
    }

    @Override
    protected void initDriver() throws MalformedURLException {
        driver = new IOSDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
    }

    protected abstract void setAppCapability();
}
