package com.android.mathias.velocity;

import java.util.Date;

public class Walk {
    private long mDuration;
    private Date mDate;
    private Route mRoute;

    public Walk (long duration, Date date, Route route) {
        mDuration = duration;
        mDate = date;
        mRoute = route;
    }

    public long getDuration() {
        return mDuration;
    }
    public void setmDuration(long duration) {
        mDuration = duration;
    }

    public Date getDate() {
        return mDate;
    }
    public void setDate(Date date) {
        mDate = date;
    }

    public Route getRoute() {
        return mRoute;
    }
    public void setRoute(Route route) {
        mRoute = route;
    }
}
