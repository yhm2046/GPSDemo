package com.adan.gpsdemo.utils;

/**
 * Data:2022.10.27
 * Describe: Class for latitude and longitude
 * Reference:https://github.com/taoweiji/JZLocationConverter-for-Android
 */
public class LatLng {
    public double latitude;
    public double longitude;

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
