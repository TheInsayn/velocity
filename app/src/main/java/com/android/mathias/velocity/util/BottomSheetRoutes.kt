package com.android.mathias.velocity.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.mathias.velocity.R
import com.android.mathias.velocity.model.Route
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetRoutes : BottomSheetDialogFragment(), OnMapReadyCallback {

    private lateinit var mTxtName: TextView
    private lateinit var mTxtFrom: TextView
    private lateinit var mTxtTo: TextView
    private lateinit var mBtnEdit: Button

    private lateinit var mRoute: Route

    private var mMiniMap: SupportMapFragment? = null

    internal fun newInstance(route: Route): BottomSheetRoutes {
        return BottomSheetRoutes().apply { mRoute = route }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.sheet_route_info, container, false)

        mTxtName = view.findViewById(R.id.txt_info_sheet_name)
        mTxtFrom = view.findViewById(R.id.txt_info_sheet_from)
        mTxtTo = view.findViewById(R.id.txt_create_sheet_to)
        mBtnEdit = view.findViewById(R.id.btn_info_sheet_edit_route)
        mMiniMap = activity!!.supportFragmentManager.findFragmentById(R.id.minimap) as SupportMapFragment?
        mTxtName.text = mRoute.name
        mTxtFrom.text = mRoute.startName
        mTxtTo.text = mRoute.endName
        mMiniMap!!.getMapAsync(this)
        return view
    }

    override fun onMapReady(map: GoogleMap) {
        val start: LatLng = mRoute.startLoc!!
        val end: LatLng = mRoute.endLoc!!
        map.addMarker(MarkerOptions().position(start))
        map.addMarker(MarkerOptions().position(end))
        val latLngBounds = arrayOf(start, end)
                .fold(LatLngBounds.Builder(), LatLngBounds.Builder::include).build()
        map.setLatLngBoundsForCameraTarget(latLngBounds)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.center, 12f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val f: Fragment? = activity!!.supportFragmentManager.findFragmentById(R.id.minimap)
        if (f != null)
            activity!!.supportFragmentManager.beginTransaction().remove(f).commit()
    }
}
