package com.spartech.worldweather.utils.save;

/**
 * Created by HQ on 03-Sep-15.
 */
public class MainFragSave {

    private String mTime;
    private String mHumidity;
    private String mLocation;
    private String mPrecip;
    private String mSummary;
    private String mTemperature;
    private String mIcon;

    public MainFragSave(String time, String humidity, String location, String precip, String summary, String temperature, String icon) {
        mTime = time;
        mHumidity = humidity;
        mLocation = location;
        mPrecip = precip;
        mSummary = summary;
        mTemperature = temperature;
        mIcon = icon;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getHumidity() {
        return mHumidity;
    }

    public void setHumidity(String humidity) {
        mHumidity = humidity;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public String getPrecip() {
        return mPrecip;
    }

    public void setPrecip(String precip) {
        mPrecip = precip;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public String getTemperature() {
        return mTemperature;
    }

    public void setTemperature(String temperature) {
        mTemperature = temperature;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }
}
