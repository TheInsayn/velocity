package com.android.mathias.velocity;

import java.util.Date;

public class Walk {
    private Route mRoute;
    private long mDuration;
    private Date mDate;

    public Walk () {
        mRoute = new Route("NOT SET");
        mDuration = 0;
        mDate = new Date();
    }
    public Walk (long duration, Date date, Route route) {
        mRoute = route;
        mDuration = duration;
        mDate = date;
    }

    public Route getRoute() {
        return mRoute;
    }
    public void setRoute(Route route) {
        mRoute = route;
    }

    public long getDuration() {
        return mDuration;
    }
    public void setDuration(long duration) {
        mDuration = duration;
    }

    public Date getDate() {
        return mDate;
    }
    public void setDate(Date date) {
        mDate = date;
    }
}
