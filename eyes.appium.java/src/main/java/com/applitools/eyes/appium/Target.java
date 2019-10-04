package com.applitools.eyes.appium;

import com.applitools.eyes.Region;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


/**
 * Created by antonchuev on 9/26/19.
 */
public class Target {
    public static EyesAppiumCheckSettings window() {
        return new EyesAppiumCheckSettings();
    }

    public static EyesAppiumCheckSettings region(Region region) {
        return new EyesAppiumCheckSettings(region);
    }

    public static EyesAppiumCheckSettings region(By by) {
        return new EyesAppiumCheckSettings(by);
    }

    public static EyesAppiumCheckSettings region(WebElement webElement) {
        return new EyesAppiumCheckSettings(webElement);
    }
}
