package com.android.mathias.velocity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;

import java.util.Date;


public class FragmentCurrent extends android.support.v4.app.Fragment {

    Chronometer mStopwatch;
    StopwatchState mStopwatchState;
    long mLastStopTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_current, container, false);
        setHasOptionsMenu(true);
        mStopwatch = (Chronometer) currentView.findViewById(R.id.stopwatch);
        FloatingActionButton fabPlayPause = (FloatingActionButton) currentView.findViewById(R.id.fab_current_play_pause);
        FloatingActionButton fabStop = (FloatingActionButton) currentView.findViewById(R.id.fab_current_stop);
        fabPlayPause.setOnClickListener(view1 -> handleFabEvent(currentView, fabPlayPause));
        fabStop.setOnClickListener(view1 -> handleFabEvent(currentView, fabStop));
        mStopwatchState = StopwatchState.STOPPED;
        return currentView;
    }

    private void handleFabEvent(View currentView, FloatingActionButton fab) {
        FloatingActionButton fabPlayPause = (FloatingActionButton) currentView.findViewById(R.id.fab_current_play_pause);
        FloatingActionButton fabStop = (FloatingActionButton) currentView.findViewById(R.id.fab_current_stop);
        if (fab.getId() == R.id.fab_current_play_pause) {
            switch (mStopwatchState) {
                case STOPPED:
                    mStopwatch.setBase(SystemClock.elapsedRealtime());
                    mStopwatch.start();
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                    mStopwatchState = StopwatchState.RUNNING;
                    fabStop.setClickable(true);
                    fabStop.setVisibility(View.VISIBLE);
                    break;
                case RUNNING:
                    mStopwatch.stop();
                    mLastStopTime = SystemClock.elapsedRealtime();
                    fab.setImageResource(android.R.drawable.ic_media_play);
                    mStopwatchState = StopwatchState.PAUSED;
                    break;
                case PAUSED:
                    mStopwatch.setBase(mStopwatch.getBase() + (SystemClock.elapsedRealtime() - mLastStopTime));
                    mStopwatch.start();
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                    mStopwatchState = StopwatchState.RUNNING;
                    break;
                default:
                    break;
            }
        } else {
            mStopwatch.stop();
            Walk walk = new Walk(SystemClock.elapsedRealtime() - mStopwatch.getBase(), new Date(), new Route("To work"));
            DBManager.saveWalk(getContext(), walk);
            mLastStopTime = 0;
            fabPlayPause.setImageResource(android.R.drawable.ic_media_play);
            mStopwatchState = StopwatchState.STOPPED;
            fabStop.setClickable(false);
            fabStop.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_current, menu);
    }

    enum StopwatchState {
        RUNNING, PAUSED, STOPPED
    }
}


