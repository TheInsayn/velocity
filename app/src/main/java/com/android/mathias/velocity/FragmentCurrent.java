package com.android.mathias.velocity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FragmentCurrent extends android.support.v4.app.Fragment {

    private static int NOTIFICATION_ID = 1;
    private static String CHANNEL_ID = "channel_walk";
    TextView mTimeView;
    TextView mRouteView;
    TimeState mTimeState;
    long mStartTime;
    long mLastStopTime;
    Route mCurrentWalkRoute;
    ObjectAnimator mAnimator;
    NotificationManager mNotificationManager;
    SharedPreferences mSharedPref;
    NotificationChannel mChannel;
    Activity mActivity;

    FloatingActionButton mFab;
    Button mBtnR;
    Handler mHandler;
    ProgressBar mProgressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_current, container, false);
        setHasOptionsMenu(true);
        return view;
    }

    private void toggleStopwatch() {
        switch (mTimeState) {
            case STOPPED: startWalk(); break;
            case RUNNING: pauseStopwatch(); break;
            case PAUSED:  resumeStopwatch(); break;
            default: break;
        }
    }

    private void startWalk() {
        final List<String> routeNames = new ArrayList<>();
        for (Route r : DBManager.getRoutes(getContext(), null)) { routeNames.add(r.getName()); }
        String defaultRouteName = mSharedPref.getString("default_route", "None");
        if (defaultRouteName.equals("None")) { mCurrentWalkRoute = new Route("No route set"); }
        else { mCurrentWalkRoute = DBManager.getRoutes(getContext(), defaultRouteName).get(0); }
        if (routeNames.size() > 0 && defaultRouteName.equals("None")) {
            final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.support_simple_spinner_dropdown_item);
            adapter.addAll(routeNames);
            adapter.add("None");
            final ListPopupWindow lpw = new ListPopupWindow(getContext());
            lpw.setAdapter(adapter);
            lpw.setAnchorView(mFab);
            lpw.setContentWidth((int) (((View)mFab.getParent()).getWidth()/2.5));
            lpw.setOnItemClickListener((adapterView, view, i, l) -> {
                if (adapterView.getItemAtPosition(i).toString().equals("None")) {
                    mCurrentWalkRoute = new Route("No route set");
                } else {
                    mCurrentWalkRoute = DBManager.getRoutes(getContext(), adapterView.getItemAtPosition(i).toString()).get(0);
                }
                startStopwatch();
                lpw.dismiss();
            });
            lpw.show();
        } else {
            startStopwatch();
        }
    }

    private void startStopwatch() {
        mStartTime = SystemClock.elapsedRealtime();
        mTimeState = TimeState.RUNNING;
        updateUI();
        mHandler = new Handler();
        mHandler.post(mRunnable);
    }

    protected void pauseStopwatch() {
        mLastStopTime = SystemClock.elapsedRealtime();
        mTimeState = TimeState.PAUSED;
        updateUI();
        mHandler.removeCallbacks(mRunnable);
        buildNotification();
    }

    protected void resumeStopwatch() {
        mStartTime = mStartTime + (SystemClock.elapsedRealtime() - mLastStopTime);
        mTimeState = TimeState.RUNNING;
        updateUI();
        mHandler.post(mRunnable);
    }

    protected void stopWalk() {
        long walkTime = SystemClock.elapsedRealtime() - mStartTime;
        Walk walk = new Walk(walkTime, new Date(), mCurrentWalkRoute);
        mLastStopTime = 0;
        mCurrentWalkRoute = null;
        mTimeState = TimeState.STOPPED;
        updateUI();
        DBManager.saveWalk(getContext(), walk);
    }

    private void updateUI() {
        switch (mTimeState) {
            case STOPPED:
                mFab.setImageResource(android.R.drawable.ic_media_play);
                mBtnR.setClickable(false);
                mBtnR.setVisibility(View.INVISIBLE);
                mRouteView.setText("");
                mProgressBar.setProgress(0);
                if (mNotificationManager.getActiveNotifications() != null) mNotificationManager.cancel(NOTIFICATION_ID);
                if (mHandler != null) mHandler.removeCallbacks(mRunnable);
                break;
            case RUNNING:
                mFab.setImageResource(android.R.drawable.ic_media_pause);
                mBtnR.setClickable(true);
                mBtnR.setVisibility(View.VISIBLE);
                mRouteView.setText(mCurrentWalkRoute.getName());
                break;
            case PAUSED:
                mFab.setImageResource(android.R.drawable.ic_media_play);
                mBtnR.setClickable(true);
                mBtnR.setVisibility(View.VISIBLE);
                mTimeView.setText(DateFormat.format("mm:ss", new Date(mLastStopTime-mStartTime)));
                mRouteView.setText(mCurrentWalkRoute.getName());
                break;
            default: break;
        }
    }

    private void buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_current)
                .setContentTitle(mTimeState == TimeState.RUNNING ? "Ongoing walk" : "Walk paused")
                .setSubText(mCurrentWalkRoute.getName())
                .setContentText(DateFormat.format("mm:ss", new Date(SystemClock.elapsedRealtime()-mStartTime)))
                .setOngoing(mTimeState == TimeState.RUNNING)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_app))
                .setAutoCancel(true);
        Intent notificationIntent = new Intent(getActivity(), ActivityMain.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        builder.setContentIntent(PendingIntent.getActivity(getActivity(), 0, notificationIntent, 0));
        addNotificationActions(builder);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void addNotificationActions(NotificationCompat.Builder builder) {
        switch (mTimeState) {
            case RUNNING:
                Intent pauseIntent = new Intent(getActivity(), ActivityMain.class);
                pauseIntent.setAction(getString(R.string.notification_action_pause));
                PendingIntent pausePendingIntent = PendingIntent.getActivity(getActivity(), 0, pauseIntent, 0);
                NotificationCompat.Action pauseAction = new NotificationCompat.Action.Builder(
                        R.drawable.ic_walk, getString(R.string.notification_action_pause), pausePendingIntent).build();
                builder.addAction(pauseAction);
                break;
            case PAUSED:
                Intent resumeIntent = new Intent(getActivity(), ActivityMain.class);
                resumeIntent.setAction(getString(R.string.notification_action_resume));
                PendingIntent resumePendingIntent = PendingIntent.getActivity(getActivity(), 0, resumeIntent, 0);
                NotificationCompat.Action resumeAction = new NotificationCompat.Action.Builder(
                        R.drawable.ic_walk, getString(R.string.notification_action_resume), resumePendingIntent).build();
                builder.addAction(resumeAction);
            default: break;
        }
        //no checks as far as TimeState.STOPPED has no notification
        Intent stopIntent = new Intent(getActivity(), ActivityMain.class);
        stopIntent.setAction(getString(R.string.notification_action_stop));
        PendingIntent stopPendingIntent = PendingIntent.getActivity(getActivity(), 0, stopIntent, 0);
        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_walk, getString(R.string.notification_action_stop), stopPendingIntent).build();
        builder.addAction(stopAction);
    }

    private void setProgressSeconds(int seconds) {
        if (mAnimator != null && mAnimator.isRunning()) mAnimator.cancel();
        mAnimator = ObjectAnimator.ofInt(mProgressBar, "progress", seconds*100);
        mAnimator.setDuration(1500);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.start();
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
                startActivity(new Intent(mActivity, ActivitySettings.class));
                break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        //initializations
        if (mActivity == null) mActivity = getActivity();
        mTimeView = Objects.requireNonNull(mActivity).findViewById(R.id.timer);
        mRouteView = mActivity.findViewById(R.id.txt_current_route);
        mProgressBar = mActivity.findViewById(R.id.progressBar);
        mFab = mActivity.findViewById(R.id.fab_current_toggle);
        mFab.setOnClickListener(view -> toggleStopwatch());
        mBtnR = mActivity.findViewById(R.id.fab_current_stop);
        mBtnR.setOnClickListener(view -> stopWalk());
        if (mTimeState == null) mTimeState = TimeState.STOPPED;
        if (mNotificationManager == null) mNotificationManager = (NotificationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.NOTIFICATION_SERVICE);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        //custom actions
        if (mTimeState == TimeState.RUNNING) mHandler.post(mRunnable);
        if (mChannel == null) createNotificationChannel();
        updateUI();
    }

    private void createNotificationChannel() {
        mChannel = new NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW);
        mChannel.setDescription(getString(R.string.channel_description));
        mChannel.enableLights(mSharedPref.getBoolean("notifications_led", false));
        mChannel.setLightColor(Color.GREEN);
        mChannel.enableVibration(mSharedPref.getBoolean("notifications_vibrate", false));
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private enum TimeState {
        RUNNING, PAUSED, STOPPED
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            long time = (SystemClock.elapsedRealtime() - mStartTime);
            int sec = (int) (TimeUnit.MILLISECONDS.toSeconds(time)) % 60;
            mTimeView.setText(DateFormat.format("mm:ss", new Date(time)));
            if (mSharedPref.getBoolean("enable_notifications", false))  {
                buildNotification();
            } else if (mNotificationManager.getActiveNotifications() != null) {
                mNotificationManager.cancel(NOTIFICATION_ID);
            }
            setProgressSeconds(sec);
            mHandler.postDelayed(this, 1000);
        }
    };
}


