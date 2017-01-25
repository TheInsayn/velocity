package com.android.mathias.velocity;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

class Route {
    private String mName;
    private LatLng mStartLoc;
    private LatLng mEndLoc;
    private String mStartName;
    private String mEndName;

    public Route() { }

    public Route(String name) {
        mName = name;
    }

    public Route(String name, LatLng startLoc, LatLng endLoc) {
        mName = name;
        mStartLoc = startLoc;
        mEndLoc = endLoc;
    }

    public Route(String name, LatLng startLoc, LatLng endLoc, String startName, String endName) {
        mName = name;
        mStartLoc = startLoc;
        mEndLoc = endLoc;
        mStartName = startName;
        mEndName = endName;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }

    LatLng getStartLoc() {
        return mStartLoc;
    }
    void setStartLoc(LatLng startLoc) {
        mStartLoc = startLoc;
    }

    LatLng getEndLoc() {
        return mEndLoc;
    }
    void setEndLoc(LatLng endLoc) {
        mEndLoc = endLoc;
    }

    String getStartName() {
        return mStartName;
    }
    void setStartName(String startName) {
        mStartName = startName;
    }

    String getEndName() {
        return mEndName;
    }
    void setEndName(String endName) {
        mEndName = endName;
    }

    float getApproximateDistance () {
        Location startLoc = new Location("start");
        startLoc.setLatitude(mStartLoc.latitude);
        startLoc.setLongitude(mStartLoc.longitude);
        Location endLoc = new Location("end");
        endLoc.setLatitude(mEndLoc.latitude);
        endLoc.setLongitude(mEndLoc.longitude);
        return startLoc.distanceTo(endLoc);
    }
}
