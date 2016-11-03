package com.android.mathias.velocity;

import android.location.Location;

public class Route {
    private String mName;
    private Location mStartPoint;
    private String mStartName;
    private Location mEndPoint;
    private String mEndName;

    public Route(String name) {
        mName = name;
    }

    public Route(String name, Location startPoint, Location endPoint) {
        mName = name;
        mStartPoint = startPoint;
        mEndPoint = endPoint;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }

    public Location getStartPoint() {
        return mStartPoint;
    }
    public void setStartPoint(Location startPoint) {
        mStartPoint = startPoint;
    }

    public Location getEndPoint() {
        return mEndPoint;
    }
    public void setEndPoint(Location endPoint) {
        mEndPoint = endPoint;
    }

    public float getApproximateDistance () {
        return mStartPoint.distanceTo(mEndPoint);
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
}
