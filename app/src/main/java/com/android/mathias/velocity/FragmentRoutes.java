package com.android.mathias.velocity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import java.util.List;

public class FragmentRoutes extends android.support.v4.app.Fragment {

    private RecyclerAdapterRoutes mAdapter;
    private final List<Route> mListRoutes = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View routesView = inflater.inflate(R.layout.fragment_routes, container, false);
        initRecyclerView(routesView);
        setHasOptionsMenu(true);
        FloatingActionButton fabCreate = (FloatingActionButton) routesView.findViewById(R.id.fab_create_route);
        fabCreate.setOnClickListener(view -> handleFabEvent(fabCreate));
        addDemoRoutes();
        List<Route> routes = DBManager.getRoutes(getContext(), null);
        for (Route r : routes) {
            addRouteCard(r);
        }
        return routesView;
    }

    private void handleFabEvent(FloatingActionButton fab) {
        startActivity(new Intent(getActivity(), ActivityCreateRoute.class));
    }

    private void addDemoRoutes() {
        Location startPoint = new Location("start");
        startPoint.setLongitude(20.5);
        startPoint.setLatitude(10.5);
        Location endPoint = new Location("end");
        endPoint.setLongitude(20.7);
        endPoint.setLatitude(10.6);
        Route route = new Route("To work", startPoint, endPoint);
        route.setStartName("Home");
        route.setEndName("Work");
        DBManager.saveRoute(getContext(), route);
    }

    private void initRecyclerView(View routesView) {
        RecyclerView rvRoutes = (RecyclerView) routesView.findViewById(R.id.list_routes);
        mAdapter = new RecyclerAdapterRoutes(mListRoutes);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvRoutes.setHasFixedSize(true);
        rvRoutes.setLayoutManager(layoutManager);
        rvRoutes.setItemAnimator(new DefaultItemAnimator());
        rvRoutes.setAdapter(mAdapter);
    }

    private void addRouteCard(Route route) {
        mListRoutes.add(route);
        mAdapter.notifyItemInserted(mListRoutes.size()-1);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_routes, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), ActivitySettings.class));
                break;
            case R.id.action_delete_routes:
                DBManager.deleteAllRoutes(getActivity());
                mListRoutes.clear();
                mAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
