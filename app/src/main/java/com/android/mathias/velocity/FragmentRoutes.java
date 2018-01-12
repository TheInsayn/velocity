package com.android.mathias.velocity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class FragmentRoutes extends android.support.v4.app.Fragment {

    private RecyclerAdapterRoutes mAdapter;
    private final List<Route> mListRoutes = new ArrayList<>();
    private Route mTempRoute = null;
    private Snackbar mSnackbar = null;

    protected static final int REQUEST_ROUTE_DATA = 200;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View routesView = inflater.inflate(R.layout.fragment_routes, container, false);
        initRecyclerView(routesView);
        setHasOptionsMenu(true);
        final FloatingActionButton fabCreate = routesView.findViewById(R.id.fab_create_route);
        fabCreate.setOnClickListener(view -> FragmentRoutes.this.handleFabEvent());
        List<Route> routes = DBManager.getRoutes(getContext(), null);
        for (Route r : routes) {
            addRouteCard(r);
        }
        return routesView;
    }

    private void handleFabEvent() {
        startActivityForResult(new Intent(getActivity(),ActivityCreateRoute.class), REQUEST_ROUTE_DATA);
    }

    private void initRecyclerView(View routesView) {
        final RecyclerView rvRoutes = routesView.findViewById(R.id.list_routes);
        mListRoutes.clear();
        mAdapter = new RecyclerAdapterRoutes(mListRoutes, rvRoutes);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplicationContext());
        rvRoutes.setLayoutManager(layoutManager);
        rvRoutes.setAdapter(mAdapter);
        final Snackbar.Callback sbCallback = new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar sb, int event) {
                if (mSnackbar != null && mTempRoute != null) {
                    DBManager.deleteRoute(getContext(), mTempRoute.getId());
                    mSnackbar.removeCallback(this);
                    mSnackbar = null;
                    mTempRoute = null;
                }
                super.onDismissed(sb, event);
            }
        };
        ItemTouchHelper.SimpleCallback ithCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                DBManager.setRoutePos(getContext(), mListRoutes.get(fromPos).getId(), toPos);
                DBManager.setRoutePos(getContext(), mListRoutes.get(toPos).getId(), fromPos);
                Collections.swap(mListRoutes, fromPos, toPos);
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (mSnackbar != null && mTempRoute != null) {
                    DBManager.deleteRoute(getContext(), mTempRoute.getId());
                    mSnackbar.removeCallback(sbCallback);
                    mSnackbar = null;
                    mTempRoute = null;
                }
                final int idx = viewHolder.getAdapterPosition();
                mTempRoute = mListRoutes.get(idx);
                mListRoutes.remove(idx);
                mAdapter.notifyItemRemoved(idx);
                mSnackbar = Snackbar.make(rvRoutes, "\"" + mTempRoute.getName() + "\" deleted.", Snackbar.LENGTH_LONG).setAction("UNDO", view -> {
                    if (mTempRoute != null) {
                        mSnackbar.removeCallback(sbCallback);
                        mListRoutes.add(idx, mTempRoute);
                        mAdapter.notifyItemInserted(idx);
                        Snackbar.make(rvRoutes, "Restored.", Snackbar.LENGTH_SHORT).show();
                        mSnackbar = null;
                        mTempRoute = null;
                    } else {
                        Snackbar.make(rvRoutes, "Error restoring...", Snackbar.LENGTH_SHORT).show();
                    }
                }).addCallback(sbCallback);
                mSnackbar.show();
            }
        };
        ItemTouchHelper ith = new ItemTouchHelper(ithCallback);
        ith.attachToRecyclerView(rvRoutes);
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
                    newRoute.setPos(mListRoutes.size()+1);
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
            case R.id.action_about:
                createRoutesDemoData();
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


    public void createRoutesDemoData() {
        for (int i = 1; i <= 5; i++) {
            LatLng startLoc = new LatLng((i+0.001) * Math.PI, (i+0.001)*Math.PI);
            LatLng endLoc = new LatLng(i * Math.PI, i*Math.PI);
            Route route = new Route("to location " + i, startLoc, endLoc, "Somewhere " + i, "Nowhere " + i);
            DBManager.saveRoute(getContext(), route);
            addRouteCard(route);
        }
    }
}
