package com.android.mathias.velocity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class ActivityCreateRoute extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

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
    private Geocoder mGeocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mapFragment.getMapAsync(this);
        Button btnSave = (Button) findViewById(R.id.btn_save_route);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptForNameAndReturn();
            }
        });
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .enableAutoManage(this, this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }
        mGeocoder = new Geocoder(this.getApplicationContext());
    }

    private void promptForNameAndReturn() {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        new AlertDialog.Builder(this)
                .setTitle("Route name")
                .setView(R.layout.dialog_route)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String routeName = ((EditText) ((Dialog) dialogInterface).findViewById(R.id.txt_name_prompt)).getText().toString();
                        finish(routeName);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                }).show();
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void finish(String routeName) {
        if (mStartLoc != null && mEndLoc != null) {
            String startLocName = "Start";
            String endLocName = "End";
            try {
                startLocName = mGeocoder.getFromLocation(mStartLoc.latitude, mStartLoc.longitude, 1).get(0).getAddressLine(0);
                endLocName = mGeocoder.getFromLocation(mEndLoc.latitude, mEndLoc.longitude, 1).get(0).getAddressLine(0);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Intent returnIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putDoubleArray(START_LOC, new double[]{mStartLoc.latitude, mStartLoc.longitude});
            bundle.putDoubleArray(END_LOC, new double[]{mEndLoc.latitude, mEndLoc.longitude});
            bundle.putString(START_LOC_NAME, startLocName);
            bundle.putString(END_LOC_NAME, endLocName);
            bundle.putString(ROUTE_NAME, routeName);
            returnIntent.putExtra(String.valueOf(RESULT_BUNDLE), bundle);
            ActivityCreateRoute.this.setResult(RESULT_OK, returnIntent);
            ActivityCreateRoute.this.finish();
        } else {
            ActivityCreateRoute.this.setResult(RESULT_CANCELED);
            ActivityCreateRoute.this.finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                ActivityCreateRoute.this.handleMapClick(mMap, latLng);
            }
        });
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) { }
            @Override
            public void onMarkerDrag(Marker marker) { }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                String address = null;
                try {
                    List<Address> addresses = mGeocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude ,1);
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e) { e.printStackTrace(); }
                if (address != null) {
                    marker.setPosition(marker.getPosition());
                    marker.setSnippet(address);
                    if (marker.getTitle().equals(getString(R.string.end_address_title))) {
                        mEndLoc = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                        ((TextView) findViewById(R.id.txt_end_address)).setText(String.format(getString(R.string.route_end_prefix), address));
                    } else {
                        mStartLoc = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                        ((TextView) findViewById(R.id.txt_start_address)).setText(String.format(getString(R.string.route_start_prefix), address));
                    }
                    marker.showInfoWindow();
                }
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateLocation();
    }

    private void handleMapClick(GoogleMap gMap, LatLng latLng) {
        try {
            List<Address> addresses = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude ,1);
            if (addresses.size() == 0) { return; }
            String address = addresses.get(0).getAddressLine(0);
            if (mStartLoc == null) {
                Marker marker = gMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.start_address_title))
                        .snippet(address)
                        .flat(true)
                        .draggable(true));
                mStartLoc = latLng;
                marker.showInfoWindow();
                TextView txtStartAddress = (TextView) findViewById(R.id.txt_start_address);
                txtStartAddress.setText(String.format(getString(R.string.route_start_prefix), address));
                txtStartAddress.setVisibility(View.VISIBLE);
            } else if (mEndLoc == null) {
                Marker marker = gMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.end_address_title))
                        .snippet(address)
                        .flat(true)
                        .draggable(true));
                mEndLoc = latLng;
                marker.showInfoWindow();
                findViewById(R.id.btn_save_route).setVisibility(View.VISIBLE);
                TextView txtEndAddress = (TextView) findViewById(R.id.txt_end_address);
                txtEndAddress.setText(String.format(getString(R.string.route_end_prefix), address));
                txtEndAddress.setVisibility(View.VISIBLE);
            } else {
                mStartLoc = null;
                mEndLoc = null;
                gMap.clear();
                findViewById(R.id.btn_save_route).setVisibility(View.GONE);
                findViewById(R.id.txt_start_address).setVisibility(View.GONE);
                findViewById(R.id.txt_end_address).setVisibility(View.GONE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                }
                // else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                // }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
