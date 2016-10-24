package com.android.mathias.velocity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FragmentHistory extends android.support.v4.app.Fragment {

    ListView walks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View historyView = inflater.inflate(R.layout.fragment_history, container, false);
        //walks = (ListView) historyView.findViewById(R.id.list_walks);
        Button btnShowWalkTime = (Button) historyView.findViewById(R.id.btn_show_time);
        btnShowWalkTime.setOnClickListener(view -> {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            long time = sharedPref.getLong("TIME", 0);
            String timeString = new SimpleDateFormat("hh:mm:ss").format(new Date(time));
            Toast.makeText(getActivity(), timeString, Toast.LENGTH_SHORT).show();
        });
        return historyView;
    }
}
