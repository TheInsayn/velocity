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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class FragmentCurrent extends android.support.v4.app.Fragment {

    Chronometer mStopwatch;
    StopwatchState mStopwatchState;
    long mLastStopTime;
    Route mCurrentWalkRoute;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View currentView = inflater.inflate(R.layout.fragment_current, container, false);
        setHasOptionsMenu(true);
        mStopwatch = (Chronometer) currentView.findViewById(R.id.stopwatch);
        final FloatingActionButton fabPlayPause = (FloatingActionButton) currentView.findViewById(R.id.fab_current_play_pause);
        final FloatingActionButton fabStop = (FloatingActionButton) currentView.findViewById(R.id.fab_current_stop);
        fabPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handlePlayPauseFabEvent(currentView, fabPlayPause);
            }
        });
        fabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleStopFabEvent(currentView, fabStop);
            }
        });
        mStopwatchState = StopwatchState.STOPPED;
        return currentView;
    }

    private void handlePlayPauseFabEvent(final View currentView, final FloatingActionButton fab) {
        switch (mStopwatchState) {
            case STOPPED:
                final List<String> routeNames = new ArrayList<>();
                for (Route r : DBManager.getRoutes(getContext(), null)) { routeNames.add(r.getName()); }
                String defaultRouteName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("default_route", "None");
                if (defaultRouteName.equals("None")) { mCurrentWalkRoute = new Route("No route set"); }
                else { mCurrentWalkRoute = DBManager.getRoutes(getContext(), defaultRouteName).get(0); }
                if (routeNames.size() > 1 && defaultRouteName.equals("None")) {
                    final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item);
                    adapter.addAll(routeNames);
                    adapter.add("None");
                    final ListPopupWindow lpw = new ListPopupWindow(getContext());
                    lpw.setAdapter(adapter);
                    lpw.setAnchorView(fab);
                    lpw.setContentWidth((int) (((View)fab.getParent()).getWidth()/2.5));
                    lpw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (adapterView.getItemAtPosition(i).toString().equals("None")) {
                                mCurrentWalkRoute = new Route("No route set");
                            } else {
                                mCurrentWalkRoute = DBManager.getRoutes(getContext(), adapterView.getItemAtPosition(i).toString()).get(0);
                            }
                            startStopwatch(currentView, fab);
                            lpw.dismiss();
                        }
                    });
                    lpw.show();
                } else {
                    startStopwatch(currentView, fab);
                }
                break;
            case RUNNING: pauseStopwatch(fab); break;
            case PAUSED:  continueStopwatch(fab); break;
            default: break;
        }
    }

    private void startStopwatch(View currentView, FloatingActionButton fab) {
        FloatingActionButton fabStop = (FloatingActionButton) currentView.findViewById(R.id.fab_current_stop);
        mStopwatch.setBase(SystemClock.elapsedRealtime());
        mStopwatch.start();
        fab.setImageResource(android.R.drawable.ic_media_pause);
        mStopwatchState = StopwatchState.RUNNING;
        fabStop.setClickable(true);
        fabStop.setVisibility(View.VISIBLE);
        ((TextView)currentView.findViewById(R.id.txt_current_route)).setText(mCurrentWalkRoute.getName());
    }

    private void pauseStopwatch(FloatingActionButton fab) {
        mStopwatch.stop();
        mLastStopTime = SystemClock.elapsedRealtime();
        fab.setImageResource(android.R.drawable.ic_media_play);
        mStopwatchState = StopwatchState.PAUSED;
    }

    private void continueStopwatch(FloatingActionButton fab) {
        mStopwatch.setBase(mStopwatch.getBase() + (SystemClock.elapsedRealtime() - mLastStopTime));
        mStopwatch.start();
        fab.setImageResource(android.R.drawable.ic_media_pause);
        mStopwatchState = StopwatchState.RUNNING;
    }

    private void handleStopFabEvent(View currentView, FloatingActionButton fab) {
        FloatingActionButton fabPlayPause = (FloatingActionButton) currentView.findViewById(R.id.fab_current_play_pause);
        mStopwatch.stop();
        long walkTime = SystemClock.elapsedRealtime() - mStopwatch.getBase();
        Walk walk = new Walk(walkTime, new Date(), mCurrentWalkRoute);
        DBManager.saveWalk(getContext(), walk);
        mLastStopTime = 0;
        fabPlayPause.setImageResource(android.R.drawable.ic_media_play);
        mStopwatchState = StopwatchState.STOPPED;
        fab.setClickable(false);
        fab.setVisibility(View.INVISIBLE);
        mCurrentWalkRoute = null;
        ((TextView)currentView.findViewById(R.id.txt_current_route)).setText("");
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
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    private enum StopwatchState {
        RUNNING, PAUSED, STOPPED
    }
}


