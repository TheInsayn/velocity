package com.android.mathias.velocity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ActivityCreateRoute extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 100;
    protected static final String ROUTE_NAME = "ROUTE_NAME";
    protected static final String START_LOC = "START_LOC";
    protected static final String END_LOC = "END_LOC";
    protected static final String START_LOC_NAME = "START_LOC_NAME";
    protected static final String END_LOC_NAME = "END_LOC_NAME";
    protected static final String RESULT_BUNDLE = "RESULT_BUNDLE";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLng mStartLoc;
    private LatLng mEndLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Button btnSave = (Button) findViewById(R.id.btn_save_route);
        btnSave.setOnClickListener(view -> promptForNameAndReturn());
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }
    }

    private void promptForNameAndReturn() {
        final String[] name = new String[1];
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this)
                .setTitle("Route name")
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    name[0] = input.getText().toString();
                    if (mStartLoc != null && mEndLoc != null) {
                        Intent returnIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putDoubleArray(START_LOC, new double[]{mStartLoc.latitude, mStartLoc.longitude});
                        bundle.putDoubleArray(END_LOC, new double[]{mEndLoc.latitude, mEndLoc.longitude});
                        bundle.putString(START_LOC_NAME, "Start");
                        bundle.putString(END_LOC_NAME, "End");
                        bundle.putString(ROUTE_NAME, name[0]);
                        returnIntent.putExtra(String.valueOf(RESULT_BUNDLE), bundle);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    } else {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(latLng -> handleMapClick(googleMap, latLng));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateLocation();
    }

    private void handleMapClick(GoogleMap gMap, LatLng latLng) {
        if (mStartLoc == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Route: start position");
            markerOptions.flat(true);
            mStartLoc = latLng;
            gMap.addMarker(markerOptions);
        } else if (mEndLoc == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Route: target location");
            markerOptions.flat(true);
            mEndLoc = latLng;
            gMap.addMarker(markerOptions);
            findViewById(R.id.btn_save_route).setVisibility(View.VISIBLE);
        } else {
            mStartLoc = null;
            mEndLoc = null;
            gMap.clear();
            findViewById(R.id.btn_save_route).setVisibility(View.GONE);
        }

    }

    private void updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_LOCATION);
        } else {
            Location mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastKnownLocation != null) {
                LatLng lastKnown = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                mMap.setMyLocationEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnown, 14f));

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder().setName("RoutePicker").setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]")).build();
        return new Action.Builder(Action.TYPE_VIEW).setObject(object).setActionStatus(Action.STATUS_TYPE_COMPLETED).build();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
    }
}
