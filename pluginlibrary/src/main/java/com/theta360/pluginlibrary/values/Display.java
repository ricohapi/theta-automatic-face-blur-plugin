package com.theta360.pluginlibrary.values;

/**
 * Display
 */
public enum Display {
    PLUGIN("plug-in"),
    BASIC("basic"),;

    private final String mDisplay;

    Display(final String display) {
        this.mDisplay = display;
    }

    @Override
    public String toString() {
        return this.mDisplay;
    }
}
