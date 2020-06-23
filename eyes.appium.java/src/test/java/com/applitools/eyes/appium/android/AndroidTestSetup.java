package com.applitools.eyes.appium.android;

import com.applitools.eyes.appium.TestSetup;
import io.appium.java_client.android.AndroidDriver;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class AndroidTestSetup extends TestSetup {

    @Override
    public void setCapabilities() {
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Google Nexus 6");
        capabilities.setCapability("platformVersion", "7.1.1");

        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("newCommandTimeout", 300);

        setAppCapability();
    }

    @Override
    protected void initDriver() throws MalformedURLException {
        driver = new AndroidDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
    }

    protected abstract void setAppCapability();
}
