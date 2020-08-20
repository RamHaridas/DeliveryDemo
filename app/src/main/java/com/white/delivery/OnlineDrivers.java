package com.white.delivery;

import com.google.firebase.firestore.GeoPoint;

public class OnlineDrivers {

    boolean assigned;
    GeoPoint geoPoint;
    String number;

    public OnlineDrivers(){}

    public boolean isAssigned() {
        return assigned;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public String getNumber() {
        return number;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
