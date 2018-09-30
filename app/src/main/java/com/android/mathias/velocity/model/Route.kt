package com.android.mathias.velocity.model

import android.content.Context
import android.location.Location
import com.android.mathias.velocity.db.DBManager

import com.google.android.gms.maps.model.LatLng

internal class Route {
    var id: Long = 0
    var pos: Int = 0
    var name: String? = null
    var startLoc: LatLng? = null
    var endLoc: LatLng? = null
    var startName: String? = null
    var endName: String? = null

    val approximateDistance: Float
        get() {
            val startLoc = Location("start")
            startLoc.latitude = startLoc.latitude
            startLoc.longitude = startLoc.longitude
            val endLoc = Location("end")
            endLoc.latitude = endLoc.latitude
            endLoc.longitude = endLoc.longitude
            return startLoc.distanceTo(endLoc)
        }

    constructor()

    constructor(name: String) {
        this.name = name
    }

    constructor(name: String, startLoc: LatLng, endLoc: LatLng, startName: String, endName: String) {
        this.name = name
        this.startLoc = startLoc
        this.endLoc = endLoc
        this.startName = startName
        this.endName = endName
    }

    constructor(id: Long, pos: Int, name: String, startLoc: LatLng, endLoc: LatLng, startName: String, endName: String) {
        this.id = id
        this.pos = pos
        this.name = name
        this.startLoc = startLoc
        this.endLoc = endLoc
        this.startName = startName
        this.endName = endName
    }

    fun getAverageWalkTime(context: Context): Long {
        var time: Long = 0
        val walks = DBManager.getWalks(context, this)
        if (walks.isNotEmpty()) {
            for (w in walks) {
                time += w.duration
            }
            time /= walks.size.toLong()
        }
        return time
    }
}
