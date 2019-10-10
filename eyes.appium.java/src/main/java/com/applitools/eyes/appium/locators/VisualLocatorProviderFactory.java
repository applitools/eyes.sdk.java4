package com.applitools.eyes.appium.locators;

import com.applitools.eyes.IServerConnector;
import com.applitools.eyes.Logger;
import com.applitools.eyes.appium.EyesAppiumDriver;
import com.applitools.eyes.appium.EyesAppiumUtils;
import com.applitools.eyes.locators.IVisualLocatorProvider;

public class VisualLocatorProviderFactory {

    private EyesAppiumDriver driver;
    private Logger logger;
    private IServerConnector serverConnector;

    public VisualLocatorProviderFactory(EyesAppiumDriver driver, Logger logger, IServerConnector serverConnector) {
        this.driver = driver;
        this.logger = logger;
        this.serverConnector = serverConnector;
    }

    public IVisualLocatorProvider getProvider() {
        if (EyesAppiumUtils.isAndroid(driver.getRemoteWebDriver())) {
            return new AndroidVisualLocatorProvider(driver, logger, serverConnector);
        } else if (EyesAppiumUtils.isIOS(driver.getRemoteWebDriver())) {
            return new IOSVisualLocatorProvider(driver, logger, serverConnector);
        }
        throw new Error("Could not find driver type for getting visual locator provider");
    }
}
