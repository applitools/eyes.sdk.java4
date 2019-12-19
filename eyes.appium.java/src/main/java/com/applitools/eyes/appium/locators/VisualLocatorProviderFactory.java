package com.applitools.eyes.appium.locators;

import com.applitools.eyes.IServerConnector;
import com.applitools.eyes.Logger;
import com.applitools.eyes.appium.EyesAppiumDriver;
import com.applitools.eyes.appium.EyesAppiumUtils;
import com.applitools.eyes.debug.DebugScreenshotsProvider;
import com.applitools.eyes.locators.IVisualLocatorProvider;

public class VisualLocatorProviderFactory {

    private EyesAppiumDriver driver;
    private Logger logger;
    private IServerConnector serverConnector;
    private DebugScreenshotsProvider debugScreenshotsProvider;

    public VisualLocatorProviderFactory(EyesAppiumDriver driver, Logger logger, IServerConnector serverConnector,
                                        DebugScreenshotsProvider debugScreenshotsProvider) {
        this.driver = driver;
        this.logger = logger;
        this.serverConnector = serverConnector;
        this.debugScreenshotsProvider = debugScreenshotsProvider;
    }

    public IVisualLocatorProvider getProvider() {
        if (EyesAppiumUtils.isAndroid(driver.getRemoteWebDriver())) {
            return new AndroidVisualLocatorProvider(driver, logger, serverConnector, debugScreenshotsProvider);
        } else if (EyesAppiumUtils.isIOS(driver.getRemoteWebDriver())) {
            return new IOSVisualLocatorProvider(driver, logger, serverConnector, debugScreenshotsProvider);
        }
        throw new Error("Could not find driver type for getting visual locator provider");
    }
}
