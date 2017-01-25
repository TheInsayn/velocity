package com.android.mathias.velocity;

import android.content.Intent;
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

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentRoutes extends android.support.v4.app.Fragment {

    private RecyclerAdapterRoutes mAdapter;
    private final List<Route> mListRoutes = new ArrayList<>();

    protected static final int REQUEST_ROUTE_DATA = 200;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View routesView = inflater.inflate(R.layout.fragment_routes, container, false);
        initRecyclerView(routesView);
        setHasOptionsMenu(true);
        final FloatingActionButton fabCreate = (FloatingActionButton) routesView.findViewById(R.id.fab_create_route);
        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentRoutes.this.handleFabEvent(fabCreate);
            }
        });
        List<Route> routes = DBManager.getRoutes(getContext(), null);
        for (Route r : routes) {
            addRouteCard(r);
        }
        return routesView;
    }

    private void handleFabEvent(FloatingActionButton fab) {
        startActivityForResult(new Intent(getActivity(),ActivityCreateRoute.class), REQUEST_ROUTE_DATA);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ROUTE_DATA) {
            if (resultCode == RESULT_OK) {
                Route newRoute = new Route();
                Bundle result = data.getBundleExtra(ActivityCreateRoute.RESULT_BUNDLE);
                double[] start = result.getDoubleArray(ActivityCreateRoute.START_LOC);
                double[] end = result.getDoubleArray(ActivityCreateRoute.END_LOC);
                if (start != null && end != null) {
                    newRoute.setName(result.getString(ActivityCreateRoute.ROUTE_NAME));
                    newRoute.setStartLoc(new LatLng(start[0], start[1]));
                    newRoute.setEndLoc(new LatLng(end[0], end[1]));
                    newRoute.setStartName(result.getString(ActivityCreateRoute.START_LOC_NAME));
                    newRoute.setEndName(result.getString(ActivityCreateRoute.END_LOC_NAME));
                    DBManager.saveRoute(getContext(), newRoute);
                    addRouteCard(newRoute);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
