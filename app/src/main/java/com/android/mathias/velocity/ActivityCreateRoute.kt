package com.android.mathias.velocity

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*

class ActivityCreateRoute : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var mMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mStartLoc: LatLng? = null
    private var mEndLoc: LatLng? = null
    private var mGeocoder: Geocoder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_route)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        mapFragment!!.getMapAsync(this)
        val btnSave = findViewById<Button>(R.id.btn_save_route)
        btnSave.setOnClickListener { promptForNameAndReturn() }
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    //TODO: .enableAutoManage(this, this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build()
        }
        mGeocoder = Geocoder(this.applicationContext)
    }

    private fun promptForNameAndReturn() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val routes = DBManager.getRoutes(this, null)
        val dialog = AlertDialog.Builder(this)
                .setView(R.layout.dialog_route)
                .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                    val routeName = ((dialogInterface as Dialog).findViewById<View>(R.id.txt_dialog_route_name) as EditText).text.toString()
                    finish(routeName)
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0) }.show()
        (Objects.requireNonNull<Any>(dialog.findViewById(R.id.txt_dialog_route_name)) as EditText).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                var match = false
                for (r in routes) {
                    match = r.name!!.contentEquals(charSequence)
                    if (match) break
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !match
                dialog.findViewById<View>(R.id.txt_dialog_name_hint)!!.visibility = if (match) View.VISIBLE else View.INVISIBLE
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun finish(routeName: String) {
        if (mStartLoc != null && mEndLoc != null) {
            var startLocName = "Start"
            var endLocName = "End"
            try {
                startLocName = mGeocoder!!.getFromLocation(mStartLoc!!.latitude, mStartLoc!!.longitude, 1)[0].getAddressLine(0)
                endLocName = mGeocoder!!.getFromLocation(mEndLoc!!.latitude, mEndLoc!!.longitude, 1)[0].getAddressLine(0)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            val returnIntent = Intent()
            val bundle = Bundle()
            bundle.putDoubleArray(START_LOC, doubleArrayOf(mStartLoc!!.latitude, mStartLoc!!.longitude))
            bundle.putDoubleArray(END_LOC, doubleArrayOf(mEndLoc!!.latitude, mEndLoc!!.longitude))
            bundle.putString(START_LOC_NAME, startLocName)
            bundle.putString(END_LOC_NAME, endLocName)
            bundle.putString(ROUTE_NAME, routeName)
            returnIntent.putExtra(RESULT_BUNDLE, bundle)
            this.setResult(RESULT_OK, returnIntent)
            finish()
        } else {
            this.setResult(RESULT_CANCELED)
            this.finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.setOnMapClickListener { latLng -> this@ActivityCreateRoute.handleMapClick(mMap, latLng) }
        mMap!!.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                var address: String? = null
                try {
                    val addresses = mGeocoder!!.getFromLocation(marker.position.latitude, marker.position.longitude, 1)
                    address = addresses[0].getAddressLine(0)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (address != null) {
                    marker.position = marker.position
                    marker.snippet = address
                    if (marker.title == getString(R.string.end_address_title)) {
                        mEndLoc = LatLng(marker.position.latitude, marker.position.longitude)
                        (findViewById<View>(R.id.txt_end_address) as TextView).text = String.format(getString(R.string.route_end_prefix), address)
                    } else {
                        mStartLoc = LatLng(marker.position.latitude, marker.position.longitude)
                        (findViewById<View>(R.id.txt_start_address) as TextView).text = String.format(getString(R.string.route_start_prefix), address)
                    }
                    marker.showInfoWindow()
                }
            }
        })
    }


    override fun onConnected(bundle: Bundle?) {
        updateLocation()
    }

    private fun handleMapClick(gMap: GoogleMap?, latLng: LatLng) {
        try {
            val addresses = mGeocoder!!.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses.size == 0) {
                return
            }
            val address = addresses[0].getAddressLine(0)
            if (mStartLoc == null) {
                val marker = gMap!!.addMarker(MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.start_address_title))
                        .snippet(address)
                        .flat(true)
                        .draggable(true))
                mStartLoc = latLng
                marker.showInfoWindow()
                val txtStartAddress = findViewById<TextView>(R.id.txt_start_address)
                txtStartAddress.text = String.format(getString(R.string.route_start_prefix), address)
                txtStartAddress.visibility = View.VISIBLE
            } else if (mEndLoc == null) {
                val marker = gMap!!.addMarker(MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.end_address_title))
                        .snippet(address)
                        .flat(true)
                        .draggable(true))
                mEndLoc = latLng
                marker.showInfoWindow()
                findViewById<View>(R.id.btn_save_route).visibility = View.VISIBLE
                val txtEndAddress = findViewById<TextView>(R.id.txt_end_address)
                txtEndAddress.text = String.format(getString(R.string.route_end_prefix), address)
                txtEndAddress.visibility = View.VISIBLE
            } else {
                mStartLoc = null
                mEndLoc = null
                gMap!!.clear()
                findViewById<View>(R.id.btn_save_route).visibility = View.GONE
                findViewById<View>(R.id.txt_start_address).visibility = View.GONE
                findViewById<View>(R.id.txt_end_address).visibility = View.GONE
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }


    }

    private fun updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_REQUEST_ACCESS_LOCATION)
        } else {
            val mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
            if (mLastKnownLocation != null) {
                val lastKnown = LatLng(mLastKnownLocation.latitude, mLastKnownLocation.longitude)
                mMap!!.isMyLocationEnabled = true
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnown, 14f))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation()
                }
                // else { permission denied, boo! Disable the functionality that depends on this permission. }
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {}

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        private const val PERMISSIONS_REQUEST_ACCESS_LOCATION = 100
        internal const val ROUTE_NAME = "ROUTE_NAME"
        internal const val START_LOC = "START_LOC"
        internal const val END_LOC = "END_LOC"
        internal const val START_LOC_NAME = "START_LOC_NAME"
        internal const val END_LOC_NAME = "END_LOC_NAME"
        internal const val RESULT_BUNDLE = "RESULT_BUNDLE"
    }
}
