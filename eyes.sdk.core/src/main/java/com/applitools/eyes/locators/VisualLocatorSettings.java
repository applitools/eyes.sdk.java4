package com.applitools.eyes.locators;

import java.util.ArrayList;
import java.util.List;

public class VisualLocatorSettings implements IVisualLocatorSettings {

    private List<String> names = new ArrayList<>();
    private boolean firsOnly = true;

    public VisualLocatorSettings() {
    }

    protected VisualLocatorSettings(String name) {
        this.names.add(name);
    }

    protected VisualLocatorSettings(List<String> names) {
        this.names.addAll(names);
    }

    public VisualLocatorSettings first() {
        VisualLocatorSettings clone = clone();
        clone.firsOnly = true;
        return clone;
    }

    public VisualLocatorSettings all() {
        VisualLocatorSettings clone = clone();
        clone.firsOnly = false;
        return clone;
    }

    public VisualLocatorSettings name(String name) {
        VisualLocatorSettings clone = clone();
        clone.names.add(name);
        return clone;
    }

    public VisualLocatorSettings names(List<String> names) {
        VisualLocatorSettings clone = clone();
        clone.names.addAll(names);
        return clone;
    }

    @Override
    protected VisualLocatorSettings clone() {
        VisualLocatorSettings settings = new VisualLocatorSettings();
        populateClone(settings);
        return settings;
    }

    private void populateClone(VisualLocatorSettings clone) {
        clone.names = this.names;
        clone.firsOnly = this.firsOnly;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public boolean isFirstOnly() {
        return firsOnly;
    }
}
