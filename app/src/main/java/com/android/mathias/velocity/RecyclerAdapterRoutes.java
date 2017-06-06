package com.android.mathias.velocity;

import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

class RecyclerAdapterRoutes extends RecyclerView.Adapter<RecyclerAdapterRoutes.RouteCardHolder> {
    private List<Route> mRouteList;
    private RecyclerView mRecyclerView;
    private int mExpandedPosition = -1;

    class RouteCardHolder extends RecyclerView.ViewHolder {
        TextView mRouteName;
        TextView mRouteStartPoint;
        TextView mRouteEndPoint;
        TextView mRouteDistance;
        RelativeLayout mExpansion;

        RouteCardHolder(View view) {
            super(view);
            mRouteName = view.findViewById(R.id.txt_route_name);
            mRouteStartPoint = view.findViewById(R.id.txt_route_start_point);
            mRouteEndPoint = view.findViewById(R.id.txt_route_end_point);
            mRouteDistance = view.findViewById(R.id.txt_route_distance);
            mExpansion = view.findViewById(R.id.route_card_expansion);
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
        holder.mRouteDistance.setText(String.format("Distance: %.1fm", route.getApproximateDistance()));
        //handle expansion in list
        final boolean isExpanded = position == mExpandedPosition;
        holder.mExpansion.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);
        holder.itemView.setOnClickListener(v -> {
            mExpandedPosition = isExpanded ? -1 : position;
            TransitionManager.beginDelayedTransition(mRecyclerView);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return mRouteList.size();
    }
}
