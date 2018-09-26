package com.android.mathias.velocity


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.google.android.gms.maps.model.LatLng
import java.text.DateFormat
import java.text.ParseException
import java.util.*

internal object DBManager {

    fun saveWalk(context: Context, walk: Walk) {
        val dbHelper = DBHelperWalks(context)
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put(WalkEntry.COLUMN_NAME_ROUTE, walk.route!!.name)
        values.put(WalkEntry.COLUMN_NAME_DURATION, walk.duration)
        values.put(WalkEntry.COLUMN_NAME_DATE, DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(walk.date))
        db.insert(WalkEntry.TABLE_NAME, null, values)
    }

    fun getWalks(context: Context, route: Route?): List<Walk> {
        val dbHelper = DBHelperWalks(context)
        val db = dbHelper.readableDatabase
        val projection = arrayOf(WalkEntry.ID, WalkEntry.COLUMN_NAME_ROUTE, WalkEntry.COLUMN_NAME_DURATION, WalkEntry.COLUMN_NAME_DATE)
        val selection = WalkEntry.COLUMN_NAME_ROUTE + " LIKE ?"
        val selectionArgs = arrayOf(if (route != null) route.name else "%")
        val sortOrder = WalkEntry.COLUMN_NAME_DATE + " DESC"
        val c = db.query(
                WalkEntry.TABLE_NAME, // The table to query
                projection, // The columns to return
                selection, // The columns for the WHERE clause
                selectionArgs, null, null, // don't filter by row groups
                sortOrder                // The sort order
        )// The values for the WHERE clause
        // don't group the rows
        val walks = ArrayList<Walk>()
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    val walkId = c.getLong(c.getColumnIndexOrThrow(WalkEntry.ID))
                    val walkRoute = Route(c.getString(c.getColumnIndexOrThrow(WalkEntry.COLUMN_NAME_ROUTE)))
                    val walkDuration = c.getLong(c.getColumnIndexOrThrow(WalkEntry.COLUMN_NAME_DURATION))
                    var walkDate = Date()
                    try {
                        val df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG)
                        walkDate = df.parse(c.getString(c.getColumnIndexOrThrow(WalkEntry.COLUMN_NAME_DATE)))
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
        val dbHelper = DBHelperWalks(context)
        val db = dbHelper.writableDatabase
        val selection = WalkEntry.ID + " = ?"
        val selectionArgs = arrayOf(id.toString())
        db.delete(WalkEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun deleteWalks(context: Context, date: Date) {
        val dbHelper = DBHelperWalks(context)
        val db = dbHelper.writableDatabase
        val selection = WalkEntry.COLUMN_NAME_DATE + " LIKE ?"
        val selectionArgs = arrayOf(date.toString())
        db.delete(WalkEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun deleteWalks(context: Context, route: Route) {
        val dbHelper = DBHelperWalks(context)
        val db = dbHelper.writableDatabase
        val selection = WalkEntry.COLUMN_NAME_ROUTE + " LIKE ?"
        val selectionArgs = arrayOf(route.name)
        db.delete(WalkEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun deleteAllWalks(context: Context) {
        val dbHelper = DBHelperWalks(context)
        val db = dbHelper.writableDatabase
        db.delete(WalkEntry.TABLE_NAME, null, null)
    }

    fun saveRoute(context: Context, route: Route) {
        val dbHelper = DBHelperRoutes(context)
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put(RouteEntry.COLUMN_NAME_NAME, route.name)
        values.put(RouteEntry.COLUMN_NAME_POS, route.pos)
        values.put(RouteEntry.COLUMN_NAME_START_LAT, route.startLoc!!.latitude)
        values.put(RouteEntry.COLUMN_NAME_START_LNG, route.startLoc!!.longitude)
        values.put(RouteEntry.COLUMN_NAME_END_LAT, route.endLoc!!.latitude)
        values.put(RouteEntry.COLUMN_NAME_END_LNG, route.endLoc!!.longitude)
        values.put(RouteEntry.COLUMN_NAME_START_NAME, route.startName)
        values.put(RouteEntry.COLUMN_NAME_END_NAME, route.endName)
        db.insert(RouteEntry.TABLE_NAME, null, values)
    }

    fun getRoutes(context: Context, name: String?): List<Route> {
        val dbHelper = DBHelperRoutes(context)
        val db = dbHelper.readableDatabase
        val projection = arrayOf(RouteEntry.ID, RouteEntry.COLUMN_NAME_POS, RouteEntry.COLUMN_NAME_NAME, RouteEntry.COLUMN_NAME_START_LAT, RouteEntry.COLUMN_NAME_START_LNG, RouteEntry.COLUMN_NAME_END_LAT, RouteEntry.COLUMN_NAME_END_LNG, RouteEntry.COLUMN_NAME_START_NAME, RouteEntry.COLUMN_NAME_END_NAME)
        val selection = RouteEntry.COLUMN_NAME_NAME + " LIKE ?"
        val selectionArgs = arrayOf(name ?: "%")
        val sortOrder = RouteEntry.COLUMN_NAME_POS + " ASC"
        val c = db.query(
                RouteEntry.TABLE_NAME, // The table to query
                projection, // The columns to return
                selection, // The columns for the WHERE clause
                selectionArgs, null, null, // don't filter by row groups
                sortOrder                // The sort order
        )// The values for the WHERE clause
        // don't group the rows
        val routes = ArrayList<Route>()
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    val routeId = c.getLong(c.getColumnIndexOrThrow(RouteEntry.ID))
                    val routePos = c.getInt(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_POS))
                    val routeName = c.getString(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_NAME))
                    val routeStartLat = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_START_LAT))
                    val routeStartLng = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_START_LNG))
                    val routeEndLat = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_END_LAT))
                    val routeEndLng = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_END_LNG))
                    val routeStartName = c.getString(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_START_NAME))
                    val routeEndName = c.getString(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_END_NAME))
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
        val dbHelper = DBHelperRoutes(context)
        val db = dbHelper.writableDatabase
        val selection = RouteEntry.ID + " = ?"
        val selectionArgs = arrayOf(id.toString())
        db.delete(RouteEntry.TABLE_NAME, selection, selectionArgs)
    }

    fun deleteAllRoutes(context: Context) {
        val dbHelper = DBHelperRoutes(context)
        val db = dbHelper.writableDatabase
        db.delete(RouteEntry.TABLE_NAME, null, null)
    }

    fun setRoutePos(context: Context, id: Long, pos: Int) {
        val dbHelper = DBHelperRoutes(context)
        val db = dbHelper.writableDatabase
        val query = "UPDATE " + RouteEntry.TABLE_NAME +
                " SET " + RouteEntry.COLUMN_NAME_POS + " = " + pos +
                " WHERE " + RouteEntry.ID + " = " + id
        db.execSQL(query)
    }

    private class DBHelperWalks internal constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(SQL_CREATE_ENTRIES)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(SQL_DELETE_ENTRIES)
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }

        companion object {
            // METADATA
            private const val DATABASE_VERSION = 1
            private const val DATABASE_NAME = "Walks.db"
            // QUERIES
            private const val TEXT_TYPE = " TEXT"
            private const val INTEGER_TYPE = " INTEGER"
            private const val COMMA_SEP = ","
            private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + WalkEntry.TABLE_NAME
            private const val SQL_CREATE_ENTRIES = "CREATE TABLE " + WalkEntry.TABLE_NAME + " (" +
                    WalkEntry.ID + " INTEGER PRIMARY KEY," +
                    WalkEntry.COLUMN_NAME_ROUTE + TEXT_TYPE + COMMA_SEP +
                    WalkEntry.COLUMN_NAME_DURATION + INTEGER_TYPE + COMMA_SEP +
                    WalkEntry.COLUMN_NAME_DATE + TEXT_TYPE + " )"
        }
    }

    private class WalkEntry : BaseColumns {
        companion object {
            const val ID = BaseColumns._ID
            const val TABLE_NAME = "walks"
            const val COLUMN_NAME_ROUTE = "route"
            const val COLUMN_NAME_DURATION = "duration"
            const val COLUMN_NAME_DATE = "date"
        }
    }

    private class DBHelperRoutes internal constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(SQL_CREATE_ENTRIES)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(SQL_DELETE_ENTRIES)
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }

        companion object {
            // METADATA
            private const val DATABASE_VERSION = 1
            private const val DATABASE_NAME = "Routes.db"
            // QUERIES
            private const val TEXT_TYPE = " TEXT"
            private const val REAL_TYPE = " REAL"
            private const val COMMA_SEP = ","
            private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME
            private const val SQL_CREATE_ENTRIES = "CREATE TABLE " + RouteEntry.TABLE_NAME + " (" +
                    RouteEntry.ID + " INTEGER PRIMARY KEY," +
                    RouteEntry.COLUMN_NAME_POS + REAL_TYPE + COMMA_SEP +
                    RouteEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    RouteEntry.COLUMN_NAME_START_LAT + REAL_TYPE + COMMA_SEP +
                    RouteEntry.COLUMN_NAME_START_LNG + REAL_TYPE + COMMA_SEP +
                    RouteEntry.COLUMN_NAME_END_LAT + REAL_TYPE + COMMA_SEP +
                    RouteEntry.COLUMN_NAME_END_LNG + REAL_TYPE + COMMA_SEP +
                    RouteEntry.COLUMN_NAME_START_NAME + TEXT_TYPE + COMMA_SEP +
                    RouteEntry.COLUMN_NAME_END_NAME + TEXT_TYPE + " )"
        }
    }

    private class RouteEntry : BaseColumns {
        companion object {
            const val ID = BaseColumns._ID
            const val TABLE_NAME = "routes"
            const val COLUMN_NAME_POS = "pos"
            const val COLUMN_NAME_NAME = "name"
            const val COLUMN_NAME_START_LAT = "start_lat"
            const val COLUMN_NAME_START_LNG = "start_lng"
            const val COLUMN_NAME_END_LAT = "end_lat"
            const val COLUMN_NAME_END_LNG = "end_lng"
            const val COLUMN_NAME_START_NAME = "start_name"
            const val COLUMN_NAME_END_NAME = "end_name"
        }
    }
}



