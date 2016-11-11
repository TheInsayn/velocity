package com.android.mathias.velocity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
        fabPlayPause.setOnClickListener(view1 -> handlePlayPauseFabEvent(currentView, fabPlayPause));
        fabStop.setOnClickListener(view1 -> handleStopFabEvent(currentView, fabStop));
        mStopwatchState = StopwatchState.STOPPED;
        return currentView;
    }

    private void handlePlayPauseFabEvent(View currentView, FloatingActionButton fab) {
        FloatingActionButton fabStop = (FloatingActionButton) currentView.findViewById(R.id.fab_current_stop);
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
    }

    private void handleStopFabEvent(View currentView, FloatingActionButton fab) {
        FloatingActionButton fabPlayPause = (FloatingActionButton) currentView.findViewById(R.id.fab_current_play_pause);
        mStopwatch.stop();
        long walkTime = SystemClock.elapsedRealtime() - mStopwatch.getBase();
        String defaultRouteName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("default_route", "None");
        Route route;
        if (defaultRouteName == "None") { route = new Route("No route set"); }
        else { route = DBManager.getRoutes(getContext(), defaultRouteName).get(0); }
        Walk walk = new Walk(walkTime, new Date(), route);
        DBManager.saveWalk(getContext(), walk);
        mLastStopTime = 0;
        fabPlayPause.setImageResource(android.R.drawable.ic_media_play);
        mStopwatchState = StopwatchState.STOPPED;
        fab.setClickable(false);
        fab.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_current, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), ActivitySettings.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    enum StopwatchState {
        RUNNING, PAUSED, STOPPED
    }
}


