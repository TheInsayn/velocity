package com.android.mathias.velocity;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

class RecyclerAdapterWalks extends RecyclerView.Adapter<RecyclerAdapterWalks.WalkCardHolder> {
    private final List<Walk> mWalkList;
    private final RecyclerView mRecyclerView;
    private int mExpandedPosition = -1;
    private int mPreviousExpandedPosition = -1;

    class WalkCardHolder extends RecyclerView.ViewHolder {
        TextView mWalkRoute;
        TextView mWalkDuration;
        TextView mWalkDate;
        TextView mWalkWeekday;
        RelativeLayout mExpansion;
        TextView mAverageTime;

        WalkCardHolder(View view) {
            super(view);
            mWalkRoute = view.findViewById(R.id.txt_walk_route);
            mWalkDuration = view.findViewById(R.id.txt_walk_duration);
            mWalkDate = view.findViewById(R.id.txt_walk_date);
            mWalkWeekday = view.findViewById(R.id.txt_walk_weekday);
            mExpansion = view.findViewById(R.id.walk_card_expansion);
            mAverageTime = view.findViewById(R.id.txt_walk_average);
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
        holder.mWalkDuration.setText(DateFormat.format("mm:ss", new Date(walk.getDuration())));
        holder.mWalkDate.setText(DateFormat.format("dd.MM.yyyy", walk.getDate()));
        holder.mWalkWeekday.setText(DateFormat.format("EEEE", walk.getDate()));
        //handle expansion in list
        final boolean isExpanded = position == mExpandedPosition;
        holder.mExpansion.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);
        if (isExpanded) mPreviousExpandedPosition = holder.getAdapterPosition();
        holder.itemView.setOnClickListener(v -> {
            mExpandedPosition = isExpanded ? -1 : holder.getAdapterPosition();
            notifyItemChanged(mPreviousExpandedPosition);
            notifyItemChanged(holder.getAdapterPosition());
        });
        if (isExpanded) {
            Date avg = new Date(walk.getRoute().getAverageWalkTime(mRecyclerView.getContext()));
            CharSequence min = DateFormat.format("m", avg);
            CharSequence sec = DateFormat.format("s", avg);
            String boldText = walk.getRoute().getName();
            String timeStr = "average walk duration for " + boldText + ": ";
            if (!min.equals("0")) timeStr += min + "m and ";
            timeStr += sec + "s";
            int idx = timeStr.indexOf(boldText);
            SpannableString str = new SpannableString(timeStr);
            str.setSpan(new StyleSpan(Typeface.BOLD), idx, idx + boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.mAverageTime.setText(str);
        }
    }

    @Override
    public int getItemCount() {
        return mWalkList.size();
    }
}
