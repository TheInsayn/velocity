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
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int idx = viewHolder.getAdapterPosition();
                mTempWalk = mListWalks.get(idx);
                mListWalks.remove(idx);
                mAdapter.notifyItemRemoved(idx);
                Snackbar.make(rvHistory, "walk deleted.", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mTempWalk != null) {
                            mListWalks.add(idx, mTempWalk);
                            mAdapter.notifyItemInserted(idx);
                            Snackbar.make(rvHistory, "restored.", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(rvHistory, "error restoring...", Snackbar.LENGTH_SHORT).show();
                        }
                        mTempWalk = null;
                    }
                }).addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        DBManager.deleteWalk(getContext(), mTempWalk.getId());
                        super.onDismissed(transientBottomBar, event);
                    }
                }).show();
            }
        };
        ItemTouchHelper ith = new ItemTouchHelper(callback);
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
}
