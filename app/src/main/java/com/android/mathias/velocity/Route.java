package com.android.mathias.velocity;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Route {
    private String mName;
    private LatLng mStartLoc;
    private LatLng mEndLoc;
    private String mStartName;
    private String mEndName;

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

    public LatLng getStartLoc() {
        return mStartLoc;
    }
    public void setStartLoc(LatLng startLoc) {
        mStartLoc = startLoc;
    }

    public LatLng getEndLoc() {
        return mEndLoc;
    }
    public void setEndLoc(LatLng endLoc) {
        mEndLoc = endLoc;
    }

    public String getStartName() {
        return mStartName;
    }
    public void setStartName(String startName) {
        mStartName = startName;
    }

    public String getEndName() {
        return mEndName;
    }
    public void setEndName(String endName) {
        mEndName = endName;
    }

    public float getApproximateDistance () {
        Location startLoc = new Location("start");
        startLoc.setLatitude(mStartLoc.latitude);
        startLoc.setLongitude(mStartLoc.longitude);
        Location endLoc = new Location("end");
        endLoc.setLatitude(mEndLoc.latitude);
        endLoc.setLongitude(mEndLoc.longitude);
        return startLoc.distanceTo(endLoc);
    }
}
