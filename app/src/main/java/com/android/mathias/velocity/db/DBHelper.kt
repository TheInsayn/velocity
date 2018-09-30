package com.android.mathias.velocity.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DBHelper(context: Context, type: HelperType)
    : SQLiteOpenHelper(context, when (type) {
    HelperType.WALKS -> DATABASE_NAME_WALKS
    HelperType.ROUTES -> DATABASE_NAME_ROUTES
}, null, DATABASE_VERSION) {

    private var createQuery: String
    private var deleteQuery: String

    init {
        when (type) {
            HelperType.WALKS -> {
                createQuery = SQL_CREATE_TABLE_WALKS
                deleteQuery = SQL_DELETE_TABLE_WALKS
            }
            HelperType.ROUTES -> {
                createQuery = SQL_CREATE_TABLE_ROUTES
                deleteQuery = SQL_DELETE_TABLE_ROUTES
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(deleteQuery)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    object WalkEntry : BaseColumns {
        const val TABLE_NAME = "walks"
        const val COL_ROUTE = "route"
        const val COL_DURATION = "duration"
        const val COL_DATE = "date"
    }

    object RouteEntry : BaseColumns {
        const val TABLE_NAME = "routes"
        const val COL_POS = "pos"
        const val COL_NAME = "name"
        const val COL_START_NAME = "start_name"
        const val COL_START_LAT = "start_lat"
        const val COL_START_LNG = "start_lng"
        const val COL_END_NAME = "end_name"
        const val COL_END_LAT = "end_lat"
        const val COL_END_LNG = "end_lng"
    }

    enum class HelperType {
        WALKS, ROUTES
    }

    companion object {
        // METADATA
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME_WALKS = "Walks.db"
        private const val DATABASE_NAME_ROUTES = "Routes.db"
        // QUERIES
        private const val SQL_DELETE_TABLE_ROUTES = "DROP TABLE IF EXISTS ${RouteEntry.TABLE_NAME}"
        private const val SQL_DELETE_TABLE_WALKS = "DROP TABLE IF EXISTS ${WalkEntry.TABLE_NAME}"
        private const val SQL_CREATE_TABLE_WALKS = "CREATE TABLE ${WalkEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${WalkEntry.COL_ROUTE} TEXT," +
                "${WalkEntry.COL_DURATION} INTEGER," +
                "${WalkEntry.COL_DATE} TEXT)"
        private const val SQL_CREATE_TABLE_ROUTES = "CREATE TABLE ${RouteEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${RouteEntry.COL_POS} REAL," +
                "${RouteEntry.COL_NAME} TEXT," +
                "${RouteEntry.COL_START_LAT} REAL," +
                "${RouteEntry.COL_START_LNG} REAL," +
                "${RouteEntry.COL_END_LAT} REAL," +
                "${RouteEntry.COL_END_LNG} REAL," +
                "${RouteEntry.COL_START_NAME} TEXT," +
                "${RouteEntry.COL_END_NAME} TEXT)"
    }
}
