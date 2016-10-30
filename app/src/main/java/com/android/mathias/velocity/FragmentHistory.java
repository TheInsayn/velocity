package com.android.mathias.velocity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FragmentHistory extends android.support.v4.app.Fragment {

    private RecyclerAdapter mAdapter;
    private final List<Walk> mListWalks = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View historyView = inflater.inflate(R.layout.fragment_history, container, false);
        initRecyclerView(historyView);
        setHasOptionsMenu(true);
        List<Walk> walks = DBManager.getWalks(getContext(), null);
        for (Walk w : walks) {
            addWalk(w.getDuration(), w.getDate(), w.getRoute());
        }
        return historyView;
    }

    private void initRecyclerView(View historyView) {
        RecyclerView rvHistory = (RecyclerView) historyView.findViewById(R.id.list_walks);
        mAdapter = new RecyclerAdapter(mListWalks);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvHistory.setHasFixedSize(true);
        rvHistory.setLayoutManager(layoutManager);
        rvHistory.setItemAnimator(new DefaultItemAnimator());
        rvHistory.setAdapter(mAdapter);
    }

    private void addWalk(long duration, Date date, Route route) {
        mListWalks.add(0, (new Walk(duration, date, route)));
        mAdapter.notifyItemInserted(0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_history, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), ActivitySettings.class));
                break;
            case R.id.action_delete_walks:
                DBManager.deleteAllData(getActivity());
                mAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
