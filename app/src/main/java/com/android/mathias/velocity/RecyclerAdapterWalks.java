package com.android.mathias.velocity;

import android.support.transition.TransitionManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

class RecyclerAdapterWalks extends RecyclerView.Adapter<RecyclerAdapterWalks.WalkCardHolder> {
    private List<Walk> mWalkList;
    private RecyclerView mRecyclerView;
    private int mExpandedPosition = -1;

    class WalkCardHolder extends RecyclerView.ViewHolder {
        TextView mWalkRoute;
        TextView mWalkDuration;
        TextView mWalkDate;
        TextView mWalkWeekday;
        CardView mCard;
        RelativeLayout mExpansion;

        WalkCardHolder(View view) {
            super(view);
            mWalkRoute = view.findViewById(R.id.txt_walk_route);
            mWalkDuration = view.findViewById(R.id.txt_walk_duration);
            mWalkDate = view.findViewById(R.id.txt_walk_date);
            mWalkWeekday = view.findViewById(R.id.txt_walk_weekday);
            mCard = view.findViewById(R.id.walk_card_view);
            mExpansion = view.findViewById(R.id.walk_card_expansion);
        }
    }

    RecyclerAdapterWalks(List<Walk> routes, RecyclerView rv) {
        mWalkList = routes;
        mRecyclerView = rv;
    }

    @Override
    public WalkCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_walk, parent, false);
        return new WalkCardHolder(view);
    }

    @Override
    public void onBindViewHolder(WalkCardHolder holder, int position) {
        Walk walk = mWalkList.get(position);
        holder.mWalkRoute.setText(walk.getRoute().getName());
        holder.mWalkDuration.setText(android.text.format.DateFormat.format("mm:ss", new Date(walk.getDuration())));
        holder.mWalkDate.setText(android.text.format.DateFormat.format("dd.MM.yyyy", walk.getDate()));
        holder.mWalkWeekday.setText(android.text.format.DateFormat.format("EEEE", walk.getDate()));
        //handle expansion in list
        final boolean isExpanded = position == mExpandedPosition;
        holder.mExpansion.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.mCard.setActivated(isExpanded);
        holder.mCard.setOnClickListener(v -> {
            mExpandedPosition = isExpanded ? -1 : position;
            TransitionManager.beginDelayedTransition(mRecyclerView);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return mWalkList.size();
    }
}
