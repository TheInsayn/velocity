package com.android.mathias.velocity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.WalkCardHolder> {
    private List<Walk> mWalkList;

    class WalkCardHolder extends RecyclerView.ViewHolder {
        TextView mWalkDuration;
        TextView mWalkDate;
        TextView mWalkRoute;

        WalkCardHolder(View view) {
            super(view);
            mWalkDuration = (TextView) view.findViewById(R.id.txt_walk_duration);
            mWalkDate = (TextView) view.findViewById(R.id.txt_walk_date);
            mWalkRoute = (TextView) view.findViewById(R.id.txt_walk_route);
        }
    }

    RecyclerAdapter(List<Walk> notes) {
        mWalkList = notes;
    }

    @Override
    public WalkCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_walk, parent, false);
        return new WalkCardHolder(view);
    }

    @Override
    public void onBindViewHolder(WalkCardHolder holder, int position) {
        Walk walk = mWalkList.get(position);
        holder.mWalkDuration.setText(android.text.format.DateFormat.format("mm:ss", new Date(walk.getDuration())));
        holder.mWalkDate.setText(android.text.format.DateFormat.format("dd.MM.yyyy", walk.getDate()));
        holder.mWalkRoute.setText(walk.getRoute().getName());
    }

    @Override
    public int getItemCount() {
        return mWalkList.size();
    }
}
