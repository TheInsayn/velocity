package com.android.mathias.velocity.model

import java.util.*

internal class Walk {
    var id: Long = 0
    var route: Route? = null
    var duration: Long = 0
    var date: Date? = null

    constructor(duration: Long, date: Date, route: Route) {
        this.route = route
        this.duration = duration
        this.date = date
    }

    constructor(id: Long, duration: Long, date: Date, route: Route) {
        this.id = id
        this.route = route
        this.duration = duration
        this.date = date
    }
}
