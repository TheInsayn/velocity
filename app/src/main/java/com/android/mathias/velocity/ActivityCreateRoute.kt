package com.android.mathias.velocity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.mathias.velocity.util.BottomSheetCreateRoutes
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class ActivityCreateRoute : AppCompatActivity(),
        OnMapReadyCallback, BottomSheetCreateRoutes.IBottomSheetListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mGeocoder: Geocoder
    private lateinit var mTxtStart: TextView
    private lateinit var mTxtEnd: TextView
    private lateinit var mBtnSave: Button

    private var mStartMarker: Marker? = null
    private var mEndMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_route)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mGeocoder = Geocoder(this)
        mTxtStart = findViewById(R.id.txt_start_address)
        mTxtEnd = findViewById(R.id.txt_end_address)
        mBtnSave = findViewById(R.id.btn_save_route)
        mBtnSave.setOnClickListener { promptForNameAndReturn() }
        mTxtStart.setOnClickListener { loadPlacePicker(REQUEST_PICK_START) }
        mTxtEnd.setOnClickListener { loadPlacePicker(REQUEST_PICK_END) }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMapClickListener { latLng -> this@ActivityCreateRoute.handleMapClick(mMap, latLng) }
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                var address: String? = null
                try {
                    val addresses = mGeocoder.getFromLocation(marker.position.latitude, marker.position.longitude, 1)
                    address = addresses[0].getAddressLine(0)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (address != null) {
                    marker.position = marker.position
                    marker.snippet = address
                    when (marker) {
                        mEndMarker -> {
                            mEndMarker!!.position = LatLng(marker.position.latitude, marker.position.longitude)
                            mTxtEnd.text = address
                        }
                        mStartMarker -> {
                            mStartMarker!!.position = LatLng(marker.position.latitude, marker.position.longitude)
                            mTxtStart.text = address
                        }
                    }
                    marker.showInfoWindow()
                }
            }
        })
        updateLocation()
    }

    private fun handleMapClick(gMap: GoogleMap?, latLng: LatLng) {
        try {
            val addresses = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses.size == 0) return
            val address = addresses[0].getAddressLine(0)
            when {
                mStartMarker == null -> {
                    mStartMarker = gMap!!.addMarker(MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.start_address_title))
                            .snippet(address)
                            .flat(true)
                            .draggable(true))
                    mStartMarker!!.showInfoWindow()
                    mTxtStart.text = address
                    mTxtStart.visibility = View.VISIBLE
                }
                mEndMarker == null -> {
                    mEndMarker = gMap!!.addMarker(MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.end_address_title))
                            .snippet(address)
                            .flat(true)
                            .draggable(true))
                    mEndMarker!!.showInfoWindow()
                    mTxtEnd.text = address
                    mTxtEnd.visibility = View.VISIBLE
                    mBtnSave.visibility = View.VISIBLE
                }
                else -> {
                    mStartMarker = null
                    mEndMarker = null
                    gMap!!.clear()
                    mBtnSave.visibility = View.GONE
                    mTxtStart.visibility = View.GONE
                    mTxtEnd.visibility = View.GONE
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }


    }

    private fun updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERM_LOCATION)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.isMyLocationEnabled = true
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }
        }
    }

    private fun loadPlacePicker(code: Int) {
        val picker = PlacePicker.IntentBuilder()
        try {
            startActivityForResult(picker.build(this@ActivityCreateRoute), code)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_START || requestCode == REQUEST_PICK_END) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                val address = //place.name.toString() + "\n" +
                        place.address.toString()
                val marker: Marker
                val txtView: TextView
                when (requestCode) {
                    REQUEST_PICK_START -> {
                        marker = mStartMarker!!
                        txtView = mTxtStart
                    }
                    else -> {
                        marker = mEndMarker!!
                        txtView = mTxtEnd
                    }
                }
                marker.position = place.latLng
                marker.snippet = address
                txtView.text = address
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERM_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun promptForNameAndReturn() {
        val sheet = BottomSheetCreateRoutes().newInstance()
        sheet.show(supportFragmentManager, "Bottom Sheet Dialog Fragment")
    }

    override fun onRouteNameSaved(text: String) {
        finish(text)
    }

    private fun finish(routeName: String) {
        if (mStartMarker != null && mEndMarker != null) {
            var startLocName = "Start"
            var endLocName = "End"
            try {
                startLocName = mGeocoder.getFromLocation(
                        mStartMarker!!.position!!.latitude,
                        mStartMarker!!.position.longitude,
                        1)[0].getAddressLine(0)
                endLocName = mGeocoder.getFromLocation(
                        mEndMarker!!.position.latitude,
                        mEndMarker!!.position.longitude,
                        1)[0].getAddressLine(0)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            val returnIntent = Intent()
            val bundle = Bundle()
            bundle.putDoubleArray(START_LOC, doubleArrayOf(mStartMarker!!.position.latitude, mStartMarker!!.position.longitude))
            bundle.putDoubleArray(END_LOC, doubleArrayOf(mEndMarker!!.position.latitude, mEndMarker!!.position.longitude))
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

    companion object {
        private const val REQUEST_PERM_LOCATION = 100
        private const val REQUEST_PICK_START = 200
        private const val REQUEST_PICK_END = 300
        internal const val ROUTE_NAME = "ROUTE_NAME"
        internal const val START_LOC = "START_LOC"
        internal const val END_LOC = "END_LOC"
        internal const val START_LOC_NAME = "START_LOC_NAME"
        internal const val END_LOC_NAME = "END_LOC_NAME"
        internal const val RESULT_BUNDLE = "RESULT_BUNDLE"
    }

}
