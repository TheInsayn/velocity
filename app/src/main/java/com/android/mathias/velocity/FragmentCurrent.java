package com.android.mathias.velocity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FragmentCurrent extends android.support.v4.app.Fragment {

    private static int NOTIFICATION_ID = 1;
    TextView mTimeView;
    TextView mRouteView;
    TimeState mTimeState;
    long mStartTime;
    long mLastStopTime;
    Route mCurrentWalkRoute;
    ObjectAnimator mAnimator;
    NotificationManager mNotificationManager;
    Activity mActivity;

    FloatingActionButton mFab;
    Button mBtnR;
    Handler mHandler;
    ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        mStartTime = SystemClock.elapsedRealtime();
        mTimeState = TimeState.RUNNING;
        updateUI();
        mHandler = new Handler();
        mHandler.post(mRunnable);
    }

    private void pauseStopwatch() {
        mLastStopTime = SystemClock.elapsedRealtime();
        mTimeState = TimeState.PAUSED;
        updateUI();
        mHandler.removeCallbacks(mRunnable);
        buildNotification();
    }

    private void resumeStopwatch() {
        mStartTime = mStartTime + (SystemClock.elapsedRealtime() - mLastStopTime);
        mTimeState = TimeState.RUNNING;
        updateUI();
        mHandler.post(mRunnable);
    }

    private void stopWalk() {
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
                if (mAnimator != null) {
                    mAnimator.cancel();
                    mAnimator.setIntValues(0);
                }
                mProgressBar.setProgress(0);
                mNotificationManager.cancel(NOTIFICATION_ID);
                if (mHandler != null) mHandler.removeCallbacks(mRunnable);
                break;
            case RUNNING:
                mFab.setImageResource(android.R.drawable.ic_media_pause);
                mBtnR.setClickable(true);
                mBtnR.setVisibility(View.VISIBLE);
                mRouteView.setText(mCurrentWalkRoute.getName());
                if (mAnimator != null) {
                    if (mAnimator.isPaused()) {
                        mAnimator.resume();
                    } else {
                       mAnimator.cancel();
                    }
                }
                break;
            case PAUSED:
                mFab.setImageResource(android.R.drawable.ic_media_play);
                mBtnR.setClickable(true);
                mBtnR.setVisibility(View.VISIBLE);
                mTimeView.setText(DateFormat.format("mm:ss", new Date(mLastStopTime-mStartTime)));
                mRouteView.setText(mCurrentWalkRoute.getName());
                mAnimator.pause();
                break;
            default: break;
        }
    }

    private void buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity)
                .setSmallIcon(R.drawable.ic_current)
                .setContentTitle(mTimeState == TimeState.RUNNING ? "Ongoing walk" : "Walk paused")
                .setSubText(mCurrentWalkRoute.getName())
                .setContentText(DateFormat.format("mm:ss", new Date(SystemClock.elapsedRealtime()-mStartTime)))
                .setOngoing(mTimeState == TimeState.RUNNING);
        //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_app));
        //builder.setAutoCancel(true);
        Intent resultIntent = new Intent(mActivity, ActivityMain.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mActivity);
        stackBuilder.addParentStack(ActivityMain.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void setProgressSeconds(int seconds) {
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
        mTimeView = (TextView) mActivity.findViewById(R.id.timer);
        mRouteView = (TextView) mActivity.findViewById(R.id.txt_current_route);
        mProgressBar = (ProgressBar) mActivity.findViewById(R.id.progressBar);
        mFab = (FloatingActionButton) mActivity.findViewById(R.id.fab_current_toggle);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleStopwatch();
            }
        });
        mBtnR = (Button) mActivity.findViewById(R.id.fab_current_stop);
        mBtnR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopWalk();
            }
        });
        if (mTimeState == null) mTimeState = TimeState.STOPPED;
        if (mNotificationManager == null) mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        //custom actions
        if (mTimeState == TimeState.RUNNING) {
            mHandler.post(mRunnable);
        }
        updateUI();
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
            buildNotification();
            setProgressSeconds(sec);
            mHandler.postDelayed(this, 1000);
        }
    };
}


