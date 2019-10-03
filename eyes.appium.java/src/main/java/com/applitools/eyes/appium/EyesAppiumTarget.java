package com.applitools.eyes.appium;

import com.applitools.eyes.Region;
import com.applitools.eyes.selenium.fluent.Target;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


/**
 * Created by antonchuev on 9/26/19.
 */
public class EyesAppiumTarget extends Target {
    public static EyesAppiumCheckSettings window()
    {
        return new EyesAppiumCheckSettings();
    }

    public static EyesAppiumCheckSettings region(Region region)
    {
        return new EyesAppiumCheckSettings(region);
    }

    public static EyesAppiumCheckSettings region(By by)
    {
        return new EyesAppiumCheckSettings(by);
    }

    public static EyesAppiumCheckSettings region(WebElement webElement)
    {
        return new EyesAppiumCheckSettings(webElement);
    }

    public static EyesAppiumCheckSettings frame(By by)
    {
        EyesAppiumCheckSettings settings = new EyesAppiumCheckSettings();
        settings = settings.frame(by);
        return settings;
    }

    public static EyesAppiumCheckSettings frame(String frameNameOrId)
    {
        EyesAppiumCheckSettings settings = new EyesAppiumCheckSettings();
        settings = settings.frame(frameNameOrId);
        return settings;
    }

    public static EyesAppiumCheckSettings frame(int index)
    {
        EyesAppiumCheckSettings settings = new EyesAppiumCheckSettings();
        settings = settings.frame(index);
        return settings;
    }

    public static EyesAppiumCheckSettings frame(WebElement webElement)
    {
        EyesAppiumCheckSettings settings = new EyesAppiumCheckSettings();
        settings = settings.frame(webElement);
        return settings;
    }
}
