package com.android.mathias.velocity;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.Locale;

class RecyclerAdapterRoutes extends RecyclerView.Adapter<RecyclerAdapterRoutes.RouteCardHolder> {
    private final List<Route> mRouteList;
    private final RecyclerView mRecyclerView;
    private int mExpandedPosition = -1;
    private int mPreviousExpandedPosition = -1;

    class RouteCardHolder extends RecyclerView.ViewHolder {
        final TextView mRouteName;
        final TextView mRouteStartPoint;
        final TextView mRouteEndPoint;
        final TextView mRouteDistance;
        final RelativeLayout mExpansion;
        final TextView mAverageTime;

        RouteCardHolder(View view) {
            super(view);
            mRouteName = view.findViewById(R.id.txt_route_name);
            mRouteStartPoint = view.findViewById(R.id.txt_route_start_point);
            mRouteEndPoint = view.findViewById(R.id.txt_route_end_point);
            mRouteDistance = view.findViewById(R.id.txt_route_distance);
            mExpansion = view.findViewById(R.id.route_card_expansion);
            mAverageTime = view.findViewById(R.id.txt_route_average);
        }
    }

    RecyclerAdapterRoutes(List<Route> routes, RecyclerView rv) {
        mRouteList = routes;
        mRecyclerView = rv;
    }

    @Override
    public RouteCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_route, parent, false);
        return new RouteCardHolder(view);
    }

    @Override
    public void onBindViewHolder(RouteCardHolder holder, int position) {
        Route route = mRouteList.get(position);
        holder.mRouteName.setText(route.getName());
        holder.mRouteStartPoint.setText(route.getStartName());
        holder.mRouteEndPoint.setText(route.getEndName());
        holder.mRouteDistance.setText(String.format(Locale.getDefault(), "Distance: %.1fm", route.getApproximateDistance()));
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
            String timeStr;
            long time = route.getAverageWalkTime(mRecyclerView.getContext());
            if (time == 0) {
                timeStr = "no walk data yet, not able to calculate average.";
            } else {
                Date avg = new Date(time);
                CharSequence min = DateFormat.format("m", avg);
                CharSequence sec = DateFormat.format("s", avg);
                timeStr = "average walk duration: ";
                if (!min.equals("0")) timeStr += min + "m and ";
                timeStr += sec + "s";
            }
            holder.mAverageTime.setText(timeStr);
        }
    }

    @Override
    public int getItemCount() {
        return mRouteList.size();
    }
}
