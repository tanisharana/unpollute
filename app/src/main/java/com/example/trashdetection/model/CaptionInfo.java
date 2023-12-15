package com.example.trashdetection.model;

public class CaptionInfo {

    String longitude;
    String latitude;
    String caption;
    String density;
    String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCaption() {
        return caption;
    }

    public String getDensity() {
        return density;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setDensity(String density) {
        this.density = density;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
