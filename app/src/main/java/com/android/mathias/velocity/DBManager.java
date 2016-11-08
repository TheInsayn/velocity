package com.android.mathias.velocity;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

final class DBManager {

    private DBManager() {}

    static void saveWalk(Context context, Walk walk) {
        DBHelperWalks dbHelper = new DBHelperWalks(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WalkEntry.COLUMN_NAME_ROUTE, walk.getRoute().getName());
        values.put(WalkEntry.COLUMN_NAME_DURATION, walk.getDuration());
        values.put(WalkEntry.COLUMN_NAME_DATE, DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG).format(walk.getDate()));
        long newRowId = db.insert(WalkEntry.TABLE_NAME, null, values);
    }

    static List<Walk> getWalks(Context context, Route route) {
        DBHelperWalks dbHelper = new DBHelperWalks(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
            WalkEntry._ID,
            WalkEntry.COLUMN_NAME_ROUTE,
            WalkEntry.COLUMN_NAME_DURATION,
            WalkEntry.COLUMN_NAME_DATE
        };
        String selection = WalkEntry.COLUMN_NAME_ROUTE + " LIKE ?";
        String[] selectionArgs = { route != null ? route.getName() : "%" };
        String sortOrder = WalkEntry.COLUMN_NAME_DATE + " DESC";
        Cursor c = db.query(
            WalkEntry.TABLE_NAME,    // The table to query
            projection,              // The columns to return
            selection,               // The columns for the WHERE clause
            selectionArgs,           // The values for the WHERE clause
            null,                    // don't group the rows
            null,                    // don't filter by row groups
            sortOrder                // The sort order
        );
        List<Walk> walks = new ArrayList<>();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    Route walkRoute = new Route(c.getString(c.getColumnIndexOrThrow(WalkEntry.COLUMN_NAME_ROUTE)));
                    long walkDuration = c.getLong(c.getColumnIndexOrThrow(WalkEntry.COLUMN_NAME_DURATION));
                    Date walkDate = new Date();
                    try {
                        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG);
                        walkDate = df.parse(c.getString(c.getColumnIndexOrThrow(WalkEntry.COLUMN_NAME_DATE)));
                    } catch (ParseException e) { e.printStackTrace(); }
                    walks.add(new Walk(walkDuration, walkDate, walkRoute));
                } while (c.moveToNext());
            }
        }
        c.close();
        return walks;
    }

    public static void deleteWalks(Context context, Date date) {
        DBHelperWalks dbHelper = new DBHelperWalks(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = WalkEntry.COLUMN_NAME_DATE + " LIKE ?";
        String[] selectionArgs = { date.toString() };
        db.delete(WalkEntry.TABLE_NAME, selection, selectionArgs);
    }
    public static void deleteWalks(Context context, Route route) {
        DBHelperWalks dbHelper = new DBHelperWalks(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = WalkEntry.COLUMN_NAME_ROUTE + " LIKE ?";
        String[] selectionArgs = { route.getName() };
        db.delete(WalkEntry.TABLE_NAME, selection, selectionArgs);
    }

    public static void deleteAllWalks(Context context) {
        DBHelperWalks dbHelper = new DBHelperWalks(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(WalkEntry.TABLE_NAME, null, null);
    }

    public static void saveRoute(Context context, Route route) {
        DBHelperRoutes dbHelper = new DBHelperRoutes(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RouteEntry.COLUMN_NAME_NAME, route.getName());
        values.put(RouteEntry.COLUMN_NAME_START_LAT, route.getStartLoc().latitude);
        values.put(RouteEntry.COLUMN_NAME_START_LNG, route.getStartLoc().longitude);
        values.put(RouteEntry.COLUMN_NAME_END_LAT, route.getEndLoc().latitude);
        values.put(RouteEntry.COLUMN_NAME_END_LNG, route.getEndLoc().longitude);
        values.put(RouteEntry.COLUMN_NAME_START_NAME, route.getStartName());
        values.put(RouteEntry.COLUMN_NAME_END_NAME, route.getEndName());
        long newRowId = db.insert(RouteEntry.TABLE_NAME, null, values);
    }

    public static List<Route> getRoutes (Context context, String name) {
        DBHelperRoutes dbHelper = new DBHelperRoutes(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
            RouteEntry._ID,
            RouteEntry.COLUMN_NAME_NAME,
            RouteEntry.COLUMN_NAME_START_LAT,
            RouteEntry.COLUMN_NAME_START_LNG,
            RouteEntry.COLUMN_NAME_END_LAT,
            RouteEntry.COLUMN_NAME_END_LNG,
            RouteEntry.COLUMN_NAME_START_NAME,
            RouteEntry.COLUMN_NAME_END_NAME
        };
        String selection = RouteEntry.COLUMN_NAME_NAME + " LIKE ?";
        String[] selectionArgs = { name != null ? name : "%" };
        String sortOrder = RouteEntry.COLUMN_NAME_NAME + " DESC";
        Cursor c = db.query(
            RouteEntry.TABLE_NAME,   // The table to query
            projection,              // The columns to return
            selection,               // The columns for the WHERE clause
            selectionArgs,           // The values for the WHERE clause
            null,                    // don't group the rows
            null,                    // don't filter by row groups
            sortOrder                // The sort order
        );
        List<Route> routes = new ArrayList<>();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    String routeName = c.getString(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_NAME));
                    double routeStartLat = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_START_LAT));
                    double routeStartLng = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_START_LNG));
                    double routeEndLat = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_END_LAT));
                    double routeEndLng = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_END_LNG));
                    String routeStartName = c.getString((c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_START_NAME)));
                    String routeEndName = c.getString((c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_END_NAME)));
                    LatLng startLoc = new LatLng(routeStartLat, routeStartLng);
                    LatLng endLoc = new LatLng(routeEndLat,routeEndLng);
                    routes.add(new Route(routeName, startLoc, endLoc, routeStartName, routeEndName));
                } while (c.moveToNext());
            }
        }
        c.close();
        return routes;
    }

    public static void deleteAllRoutes(Context context) {
        DBHelperRoutes dbHelper = new DBHelperRoutes(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(RouteEntry.TABLE_NAME, null, null);
    }

    private static final class DBHelperWalks extends SQLiteOpenHelper {
        // METADATA
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "Walks.db";
        // QUERIES
        private static final String TEXT_TYPE = " TEXT";
        private static final String INTEGER_TYPE = " INTEGER";
        private static final String COMMA_SEP = ",";
        private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + WalkEntry.TABLE_NAME;
        private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + WalkEntry.TABLE_NAME + " (" +
                    WalkEntry._ID + " INTEGER PRIMARY KEY," +
                    WalkEntry.COLUMN_NAME_ROUTE + TEXT_TYPE + COMMA_SEP +
                    WalkEntry.COLUMN_NAME_DURATION + INTEGER_TYPE +COMMA_SEP +
                    WalkEntry.COLUMN_NAME_DATE + TEXT_TYPE + " )";

        DBHelperWalks(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static class WalkEntry implements BaseColumns {
        private static final String TABLE_NAME = "walks";
        private static final String COLUMN_NAME_ROUTE = "route";
        private static final String COLUMN_NAME_DURATION = "duration";
        private static final String COLUMN_NAME_DATE = "date";
    }

    private static final class DBHelperRoutes extends SQLiteOpenHelper {
        // METADATA
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "Routes.db";
        // QUERIES
        private static final String TEXT_TYPE = " TEXT";
        private static final String REAL_TYPE = " REAL";
        private static final String COMMA_SEP = ",";
        private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME;
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + RouteEntry.TABLE_NAME + " (" +
                        RouteEntry._ID + " INTEGER PRIMARY KEY," +
                        RouteEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                        RouteEntry.COLUMN_NAME_START_LAT + REAL_TYPE +COMMA_SEP +
                        RouteEntry.COLUMN_NAME_START_LNG + REAL_TYPE +COMMA_SEP +
                        RouteEntry.COLUMN_NAME_END_LAT + REAL_TYPE +COMMA_SEP +
                        RouteEntry.COLUMN_NAME_END_LNG + REAL_TYPE +COMMA_SEP +
                        RouteEntry.COLUMN_NAME_START_NAME + TEXT_TYPE +COMMA_SEP +
                        RouteEntry.COLUMN_NAME_END_NAME + TEXT_TYPE + " )";

        DBHelperRoutes(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static class RouteEntry implements BaseColumns {
        private static final String TABLE_NAME = "routes";
        private static final String COLUMN_NAME_NAME = "name";
        private static final String COLUMN_NAME_START_LAT = "start_lat";
        private static final String COLUMN_NAME_START_LNG = "start_lng";
        private static final String COLUMN_NAME_END_LAT = "end_lat";
        private static final String COLUMN_NAME_END_LNG = "end_lng";
        private static final String COLUMN_NAME_START_NAME = "start_name";
        private static final String COLUMN_NAME_END_NAME = "end_name";
    }
}



