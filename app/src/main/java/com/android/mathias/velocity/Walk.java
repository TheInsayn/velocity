package com.android.mathias.velocity;

import java.util.Date;

class Walk {
    private Route mRoute;
    private long mDuration;
    private Date mDate;

    public Walk () {
        mRoute = new Route("NOT SET");
        mDuration = 0;
        mDate = new Date();
    }
    Walk (long duration, Date date, Route route) {
        mRoute = route;
        mDuration = duration;
        mDate = date;
    }

    Route getRoute() {
        return mRoute;
    }
    void setRoute(Route route) {
        mRoute = route;
    }

    long getDuration() {
        return mDuration;
    }
    void setDuration(long duration) {
        mDuration = duration;
    }

    Date getDate() {
        return mDate;
    }
    void setDate(Date date) {
        mDate = date;
    }
}
