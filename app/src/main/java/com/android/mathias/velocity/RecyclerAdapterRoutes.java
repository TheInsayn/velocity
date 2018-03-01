package com.android.mathias.velocity;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.Locale;

class RecyclerAdapterRoutes extends RecyclerView.Adapter<RecyclerAdapterRoutes.RouteCardHolder> {
    private final List<Route> mRouteList;
    private final RecyclerView mRecyclerView;
    private IClickInterface mClickInterface;
    private int mExpandedPosition = -1;
    private int mPreviousExpandedPosition = -1;
    private boolean mRearrangeMode = false;

    class RouteCardHolder extends RecyclerView.ViewHolder {
        final TextView mRouteName;
        final TextView mRouteStartPoint;
        final TextView mRouteEndPoint;
        final TextView mRouteDistance;
        final RelativeLayout mExpansion;
        final TextView mAverageTime;
        final ImageView mDragPoint;

        RouteCardHolder(View view) {
            super(view);
            mRouteName = view.findViewById(R.id.txt_route_name);
            mRouteStartPoint = view.findViewById(R.id.txt_route_start_point);
            mRouteEndPoint = view.findViewById(R.id.txt_route_end_point);
            mRouteDistance = view.findViewById(R.id.txt_route_distance);
            mExpansion = view.findViewById(R.id.route_card_expansion);
            mAverageTime = view.findViewById(R.id.txt_route_average);
            mDragPoint = view.findViewById(R.id.btn_drag_route);
        }
    }

    RecyclerAdapterRoutes(List<Route> routes, RecyclerView rv, IClickInterface clickInterface) {
        mRouteList = routes;
        mRecyclerView = rv;
        mClickInterface = clickInterface;
    }

    @Override
    public RouteCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_route, parent, false);
        return new RouteCardHolder(view);
    }

    @Override
    public void onBindViewHolder(RouteCardHolder holder, int position) {
        Route route = mRouteList.get(position);
        // set max width for TextViews (for making ellipsis in case)
        int halfWidth = mRecyclerView.getWidth() / 2 - 60;
        holder.mRouteName.setMaxWidth(halfWidth);
        holder.mRouteDistance.setMaxWidth(halfWidth);
        holder.mRouteStartPoint.setMaxWidth(halfWidth);
        holder.mRouteEndPoint.setMaxWidth(halfWidth);
        // fill TextViews
        holder.mRouteName.setText(route.getName());
        holder.mRouteStartPoint.setText(route.getStartName());
        holder.mRouteEndPoint.setText(route.getEndName());
        holder.mRouteDistance.setText(String.format(Locale.getDefault(), "Distance: %.1fm", route.getApproximateDistance()));
        //handle expansion in list
        final boolean isExpanded = position == mExpandedPosition;
        holder.mDragPoint.setVisibility(mRearrangeMode ? View.VISIBLE : View.INVISIBLE);
        holder.mExpansion.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);
        if (isExpanded) mPreviousExpandedPosition = holder.getAdapterPosition();
        holder.itemView.setOnClickListener(v -> {
            if (!mRearrangeMode) {
                mExpandedPosition = isExpanded ? -1 : holder.getAdapterPosition();
                notifyItemChanged(mPreviousExpandedPosition);
                notifyItemChanged(holder.getAdapterPosition());
            }
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
        holder.itemView.setOnLongClickListener(v -> {
            mClickInterface.itemLongClick(v, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mRouteList.size();
    }

    void setRearrangeMode(boolean enabled) {
        mRearrangeMode = enabled;
        mExpandedPosition = -1;
        mPreviousExpandedPosition = -1;
        notifyDataSetChanged();
    }
}
