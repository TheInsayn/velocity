package com.android.mathias.velocity;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

class Route {
    private long mId;
    private int mPos;
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

    public Route(long id, int pos, String name, LatLng startLoc, LatLng endLoc, String startName, String endName) {
        mId = id;
        mPos = pos;
        mName = name;
        mStartLoc = startLoc;
        mEndLoc = endLoc;
        mStartName = startName;
        mEndName = endName;
    }

    long getId() {
        return mId;
    }
    void setId(long id) {
        mId = id;
    }

    int getPos() {
        return mPos;
    }
    void setPos(int mPos) {
        this.mPos = mPos;
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

    long getAverageWalkTime(Context context) {
        long time = 0;
        List<Walk> walks = DBManager.getWalks(context, this);
        if (walks.size() > 0) {
            for (Walk w : walks) {
                time += w.getDuration();
            }
            time /= walks.size();
        }
        return time;
    }
}
