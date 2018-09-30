package com.android.mathias.velocity.db


import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import com.android.mathias.velocity.model.Route
import com.android.mathias.velocity.model.Walk
import com.android.mathias.velocity.db.DBHelper.*
import com.google.android.gms.maps.model.LatLng
import java.text.DateFormat
import java.text.ParseException
import java.util.*

internal object DBManager {

    fun saveWalk(context: Context, walk: Walk) {
        val dbHelper = DBHelper(context, HelperType.WALKS)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(WalkEntry.COL_ROUTE, walk.route!!.name)
            put(WalkEntry.COL_DURATION, walk.duration)
            put(WalkEntry.COL_DATE, DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(walk.date))
        }
        db.insert(WalkEntry.TABLE_NAME, null, values)
    }

    fun getWalks(context: Context, route: Route?): List<Walk> {
        val dbHelper = DBHelper(context, HelperType.WALKS)
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
                BaseColumns._ID,
                WalkEntry.COL_ROUTE,
                WalkEntry.COL_DURATION,
                WalkEntry.COL_DATE)
        val selection = WalkEntry.COL_ROUTE + " LIKE ?"
        val selectionArgs = arrayOf(if (route != null) route.name else "%")
        val sortOrder = WalkEntry.COL_DATE + " DESC"
        val c = db.query(WalkEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
        val walks = ArrayList<Walk>()
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    val walkId = c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID))
                    val walkRoute = Route(c.getString(c.getColumnIndexOrThrow(WalkEntry.COL_ROUTE)))
                    val walkDuration = c.getLong(c.getColumnIndexOrThrow(WalkEntry.COL_DURATION))
                    var walkDate = Date()
                    try {
                        val df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG)
                        walkDate = df.parse(c.getString(c.getColumnIndexOrThrow(WalkEntry.COL_DATE)))
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }

                    walks.add(Walk(walkId, walkDuration, walkDate, walkRoute))
                } while (c.moveToNext())
            }
            c.close()
        }
        return walks
    }

    fun deleteWalk(context: Context, id: Long) {
        val dbHelper = DBHelper(context, HelperType.WALKS)
        val db = dbHelper.writableDatabase
        val selection = BaseColumns._ID + " = ?"
        val selectionArgs = arrayOf(id.toString())
        db.delete(WalkEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun deleteWalks(context: Context, date: Date) {
        val dbHelper = DBHelper(context, HelperType.WALKS)
        val db = dbHelper.writableDatabase
        val selection = WalkEntry.COL_DATE + " LIKE ?"
        val selectionArgs = arrayOf(date.toString())
        db.delete(WalkEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun deleteWalks(context: Context, route: Route) {
        val dbHelper = DBHelper(context, HelperType.WALKS)
        val db = dbHelper.writableDatabase
        val selection = WalkEntry.COL_ROUTE + " LIKE ?"
        val selectionArgs = arrayOf(route.name)
        db.delete(WalkEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun deleteAllWalks(context: Context) {
        val dbHelper = DBHelper(context, HelperType.WALKS)
        val db = dbHelper.writableDatabase
        db.delete(WalkEntry.TABLE_NAME, null, null)
    }

    fun saveRoute(context: Context, route: Route) {
        val dbHelper = DBHelper(context, HelperType.ROUTES)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(RouteEntry.COL_NAME, route.name)
            put(RouteEntry.COL_POS, route.pos)
            put(RouteEntry.COL_START_LAT, route.startLoc!!.latitude)
            put(RouteEntry.COL_START_LNG, route.startLoc!!.longitude)
            put(RouteEntry.COL_END_LAT, route.endLoc!!.latitude)
            put(RouteEntry.COL_END_LNG, route.endLoc!!.longitude)
            put(RouteEntry.COL_START_NAME, route.startName)
            put(RouteEntry.COL_END_NAME, route.endName)
        }
        db.insert(RouteEntry.TABLE_NAME, null, values)
    }

    fun getRoutes(context: Context, name: String?): List<Route> {
        val dbHelper = DBHelper(context, HelperType.ROUTES)
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
                BaseColumns._ID,
                RouteEntry.COL_POS,
                RouteEntry.COL_NAME,
                RouteEntry.COL_START_LAT,
                RouteEntry.COL_START_LNG,
                RouteEntry.COL_END_LAT,
                RouteEntry.COL_END_LNG,
                RouteEntry.COL_START_NAME,
                RouteEntry.COL_END_NAME)
        val selection = RouteEntry.COL_NAME + " LIKE ?"
        val selectionArgs = arrayOf(name ?: "%")
        val sortOrder = RouteEntry.COL_POS + " ASC"
        val c = db.query(RouteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
        val routes = ArrayList<Route>()
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    val routeId = c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID))
                    val routePos = c.getInt(c.getColumnIndexOrThrow(RouteEntry.COL_POS))
                    val routeName = c.getString(c.getColumnIndexOrThrow(RouteEntry.COL_NAME))
                    val routeStartLat = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COL_START_LAT))
                    val routeStartLng = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COL_START_LNG))
                    val routeEndLat = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COL_END_LAT))
                    val routeEndLng = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COL_END_LNG))
                    val routeStartName = c.getString(c.getColumnIndexOrThrow(RouteEntry.COL_START_NAME))
                    val routeEndName = c.getString(c.getColumnIndexOrThrow(RouteEntry.COL_END_NAME))
                    val routeStartLoc = LatLng(routeStartLat, routeStartLng)
                    val routeEndLoc = LatLng(routeEndLat, routeEndLng)
                    routes.add(Route(routeId, routePos, routeName, routeStartLoc, routeEndLoc, routeStartName, routeEndName))
                } while (c.moveToNext())
            }
            c.close()
        }
        return routes
    }

    fun deleteRoute(context: Context, id: Long) {
        val dbHelper = DBHelper(context, HelperType.ROUTES)
        val db = dbHelper.writableDatabase
        val selection = BaseColumns._ID + " = ?"
        val selectionArgs = arrayOf(id.toString())
        db.delete(RouteEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun deleteAllRoutes(context: Context) {
        val dbHelper = DBHelper(context, HelperType.ROUTES)
        val db = dbHelper.writableDatabase
        db.delete(RouteEntry.TABLE_NAME, null, null)
    }

    fun setRoutePos(context: Context, id: Long, pos: Int) {
        val dbHelper = DBHelper(context, HelperType.ROUTES)
        val db = dbHelper.writableDatabase
        val query = "UPDATE ${RouteEntry.TABLE_NAME} " +
                "SET ${RouteEntry.COL_POS} = $pos " +
                "WHERE ${BaseColumns._ID} = $id"
        db.execSQL(query)
    }

}



