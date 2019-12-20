package com.applitools.eyes.appium.locators;

import com.applitools.eyes.IServerConnector;
import com.applitools.eyes.Logger;
import com.applitools.eyes.Region;
import com.applitools.eyes.appium.EyesAppiumDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroidVisualLocatorProvider extends BaseVisualLocatorProvider {

    AndroidVisualLocatorProvider(EyesAppiumDriver driver, Logger logger, IServerConnector serverConnector) {
        super(driver, serverConnector, logger);
    }

    @Override
    public Map<String, List<Region>> adjustVisualLocators(Map<String, List<Region>> map) {
        logger.verbose("Adjust visual locators size and coordinates according to device pixel ratio = " + getDevicePixelRatio());
        Map<String, List<Region>> result = new HashMap<>();
        for (String key : map.keySet()) {
            List<Region> regions = new ArrayList<>();
            if (map.get(key) != null) {
                for (Region region : map.get(key)) {
                    region.setLeft((int) (region.getLeft() * getDevicePixelRatio()));
                    region.setTop((int) (region.getTop() * getDevicePixelRatio()));
                    region.setHeight((int) (region.getHeight() * getDevicePixelRatio()));
                    region.setWidth((int) (region.getWidth() * getDevicePixelRatio()));
                    regions.add(region);
                }
                result.put(key, regions);
            }
        }
        return result;
    }
}
