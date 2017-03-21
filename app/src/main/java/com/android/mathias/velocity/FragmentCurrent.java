package com.android.mathias.velocity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class FragmentCurrent extends android.support.v4.app.Fragment {

    private static int NOTIFICATION_ID = 1;
    Chronometer mChronometer;
    ChronometerState mChronometerState;
    long mLastStopTime;
    Route mCurrentWalkRoute;
    ObjectAnimator mAnimator;
    NotificationManager mNotificationManager;

    FloatingActionButton mFab;
    Button mBtnR;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_current, container, false);
        setHasOptionsMenu(true);
        mChronometer = (Chronometer) view.findViewById(R.id.stopwatch);
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                buildNotification();
            }
        });
        mChronometerState = ChronometerState.STOPPED;
        mFab = (FloatingActionButton) view.findViewById(R.id.fab_current_play_pause);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleStopwatch();
            }
        });
        mBtnR = (Button) view.findViewById(R.id.fab_current_stop);
        mBtnR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopWalk();
            }
        });
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 600);
        mAnimator.setDuration(120000);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        return view;
    }

    private void toggleStopwatch() {
        switch (mChronometerState) {
            case STOPPED: startWalk(); break;
            case RUNNING: pauseStopwatch(); break;
            case PAUSED:  resumeStopwatch(); break;
            default: break;
        }
    }

    private void startWalk() {
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
            lpw.setAnchorView(mFab);
            lpw.setContentWidth((int) (((View)mFab.getParent()).getWidth()/2.5));
            lpw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (adapterView.getItemAtPosition(i).toString().equals("None")) {
                        mCurrentWalkRoute = new Route("No route set");
                    } else {
                        mCurrentWalkRoute = DBManager.getRoutes(getContext(), adapterView.getItemAtPosition(i).toString()).get(0);
                    }
                    startStopwatch();
                    lpw.dismiss();
                }
            });
            lpw.show();
        } else {
            startStopwatch();
        }
    }

    private void startStopwatch() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        mFab.setImageResource(android.R.drawable.ic_media_pause);
        mChronometerState = ChronometerState.RUNNING;
        mBtnR.setClickable(true);
        mBtnR.setVisibility(View.VISIBLE);
        ((TextView)getActivity().findViewById(R.id.txt_current_route)).setText(mCurrentWalkRoute.getName());
        mAnimator.start();
        buildNotification();
    }

    private void pauseStopwatch() {
        mChronometer.stop();
        mLastStopTime = SystemClock.elapsedRealtime();
        mFab.setImageResource(android.R.drawable.ic_media_play);
        mChronometerState = ChronometerState.PAUSED;
        mAnimator.pause();
        buildNotification();
    }

    private void resumeStopwatch() {
        mChronometer.setBase(mChronometer.getBase() + (SystemClock.elapsedRealtime() - mLastStopTime));
        mChronometer.start();
        mFab.setImageResource(android.R.drawable.ic_media_pause);
        mChronometerState = ChronometerState.RUNNING;
        mAnimator.resume();
        buildNotification();
    }

    private void stopWalk() {
        mChronometer.stop();
        long walkTime = SystemClock.elapsedRealtime() - mChronometer.getBase();
        Walk walk = new Walk(walkTime, new Date(), mCurrentWalkRoute);
        DBManager.saveWalk(getContext(), walk);
        mLastStopTime = 0;
        mFab.setImageResource(android.R.drawable.ic_media_play);
        mChronometerState = ChronometerState.STOPPED;
        mBtnR.setClickable(false);
        mBtnR.setVisibility(View.INVISIBLE);
        mCurrentWalkRoute = null;
        ((TextView)getActivity().findViewById(R.id.txt_current_route)).setText("");
        mAnimator.cancel();
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private void buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_current)
                .setContentTitle(mChronometerState == ChronometerState.RUNNING ? "Ongoing walk" : "Walk paused")
                .setSubText(mCurrentWalkRoute.getName())
                .setContentText(android.text.format.DateFormat.format("mm:ss", new Date((SystemClock.elapsedRealtime()-mChronometer.getBase()))))
                //.setProgress(600, (int) mAnimator.getAnimatedValue(), false)
                .setOngoing(true);
        //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_app));
        //builder.setAutoCancel(true);
        Intent resultIntent = new Intent(getActivity(), ActivityMain.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
        stackBuilder.addParentStack(ActivityMain.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
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

    private enum ChronometerState {
        RUNNING, PAUSED, STOPPED
    }
}


