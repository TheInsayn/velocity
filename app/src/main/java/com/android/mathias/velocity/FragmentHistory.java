package com.android.mathias.velocity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FragmentHistory extends android.support.v4.app.Fragment {

    private RecyclerAdapterWalks mAdapter;
    private final List<Walk> mListWalks = new ArrayList<>();
    private Walk mTempWalk = null;
    private Snackbar mSnackbar = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View historyView = inflater.inflate(R.layout.fragment_history, container, false);
        initRecyclerView(historyView);
        setHasOptionsMenu(true);
        List<Walk> walks = DBManager.getWalks(getContext(), null);
        for (Walk w : walks) {
            addWalkCard(w);
        }
        return historyView;
    }

    private void initRecyclerView(View historyView) {
        final RecyclerView rvHistory = (RecyclerView) historyView.findViewById(R.id.list_walks);
        mAdapter = new RecyclerAdapterWalks(mListWalks);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvHistory.setHasFixedSize(true);
        rvHistory.setLayoutManager(layoutManager);
        rvHistory.setItemAnimator(new DefaultItemAnimator());
        rvHistory.setAdapter(mAdapter);
        final Snackbar.Callback sbCallback = new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar sb, int event) {
                if (mSnackbar != null && mTempWalk != null) {
                    DBManager.deleteWalk(getContext(), mTempWalk.getId());
                    mSnackbar.removeCallback(this);
                    mSnackbar = null;
                    mTempWalk = null;
                }
                super.onDismissed(sb, event);
            }
        };
        ItemTouchHelper.SimpleCallback ithCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (mSnackbar != null && mTempWalk != null) {
                    DBManager.deleteWalk(getContext(), mTempWalk.getId());
                    mSnackbar.removeCallback(sbCallback);
                    mSnackbar = null;
                    mTempWalk = null;
                }
                final int idx = viewHolder.getAdapterPosition();
                mTempWalk = mListWalks.get(idx);
                mListWalks.remove(idx);
                mAdapter.notifyItemRemoved(idx);
                mSnackbar = Snackbar.make(rvHistory, "Walk deleted.", Snackbar.LENGTH_LONG).setAction("UNDO", view -> {
                    if (mTempWalk != null) {
                        mSnackbar.removeCallback(sbCallback);
                        mListWalks.add(idx, mTempWalk);
                        mAdapter.notifyItemInserted(idx);
                        Snackbar.make(rvHistory, "Restored.", Snackbar.LENGTH_SHORT).show();
                        mSnackbar = null;
                        mTempWalk = null;
                    } else {
                        Snackbar.make(rvHistory, "Error restoring...", Snackbar.LENGTH_SHORT).show();
                    }
                }).addCallback(sbCallback);
                mSnackbar.show();
            }
        };
        ItemTouchHelper ith = new ItemTouchHelper(ithCallback);
        ith.attachToRecyclerView(rvHistory);
    }

    private void addWalkCard(Walk walk) {
        mListWalks.add(walk);
        mAdapter.notifyItemInserted(mListWalks.size()-1);
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
                DBManager.deleteAllWalks(getActivity());
                mListWalks.clear();
                mAdapter.notifyDataSetChanged();
                break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }
        super.onDetach();
    }
}
