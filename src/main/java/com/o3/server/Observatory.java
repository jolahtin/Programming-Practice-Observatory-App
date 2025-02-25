package com.o3.server;

public class Observatory {
    private String observatoryName;
    private double latitude;
    private double longitude;

    public String getObservatoryName() {
        return observatoryName;
    }
    public void setObservatoryName(String observatoryName) {
        this.observatoryName = observatoryName;
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
