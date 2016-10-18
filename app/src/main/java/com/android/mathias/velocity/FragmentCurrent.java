package com.android.mathias.velocity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;


public class FragmentCurrent extends android.support.v4.app.Fragment {

    Chronometer mStopwatch;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_current, container, false);
        Button startButton = (Button) currentView.findViewById(R.id.btn_stopwatch_start);
        mStopwatch = (Chronometer) currentView.findViewById(R.id.stopwatch);
        startButton.setOnClickListener(view1 -> {
            mStopwatch.setBase(SystemClock.elapsedRealtime());
            mStopwatch.start();
        });
        Button stopButton = (Button) currentView.findViewById(R.id.btn_stopwatch_stop);
        stopButton.setOnClickListener(view1 -> {
            mStopwatch.stop();
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong("TIME", SystemClock.elapsedRealtime() - mStopwatch.getBase()).apply();
        });

        return currentView;
    }
}
