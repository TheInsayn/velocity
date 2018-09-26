package com.android.mathias.velocity

import java.util.*

internal class Walk {
    var id: Long = 0
    var route: Route? = null
    var duration: Long = 0
    var date: Date? = null

    constructor() {
        route = Route("NOT SET")
        duration = 0
        date = Date()
    }

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
