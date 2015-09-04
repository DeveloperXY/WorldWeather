package com.spartech.worldweather.utils.save;

/**
 * Created by HQ on 04-Sep-15.
 */
public class MainActivitySave {

    private boolean mDrawerIsOpen;

    public MainActivitySave(boolean drawerIsOpen) {
        mDrawerIsOpen = drawerIsOpen;
    }

    public boolean isDrawerOpen() {
        return mDrawerIsOpen;
    }

    public void setDrawerIsOpen(boolean drawerIsOpen) {
        mDrawerIsOpen = drawerIsOpen;
    }
}
