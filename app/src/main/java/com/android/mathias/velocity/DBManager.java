package com.android.mathias.velocity;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.provider.BaseColumns;

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
        values.put(RouteEntry.COLUMN_NAME_STARTLOC_ALT, route.getStartPoint().getAltitude());
        values.put(RouteEntry.COLUMN_NAME_STARTLOC_LONG, route.getStartPoint().getLongitude());
        values.put(RouteEntry.COLUMN_NAME_ENDLOC_ALT, route.getEndPoint().getAltitude());
        values.put(RouteEntry.COLUMN_NAME_ENDLOC_LONG, route.getEndPoint().getLongitude());
        values.put(RouteEntry.COLUMN_NAME_STARTLOC_NAME, route.getStartName());
        values.put(RouteEntry.COLUMN_NAME_ENDLOC_NAME, route.getEndName());
        long newRowId = db.insert(RouteEntry.TABLE_NAME, null, values);
    }

    public static List<Route> getRoutes (Context context, String name) {
        DBHelperRoutes dbHelper = new DBHelperRoutes(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
            RouteEntry._ID,
            RouteEntry.COLUMN_NAME_NAME,
            RouteEntry.COLUMN_NAME_STARTLOC_ALT,
            RouteEntry.COLUMN_NAME_STARTLOC_LONG,
            RouteEntry.COLUMN_NAME_ENDLOC_ALT,
            RouteEntry.COLUMN_NAME_ENDLOC_LONG,
            RouteEntry.COLUMN_NAME_STARTLOC_NAME,
            RouteEntry.COLUMN_NAME_ENDLOC_NAME
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
                    double routeStartLocAlt = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_STARTLOC_ALT));
                    double routeStartLocLong = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_STARTLOC_LONG));
                    double routeEndLocAlt = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_ENDLOC_ALT));
                    double routeEndLocLong = c.getDouble(c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_ENDLOC_LONG));
                    String routeStartLocName = c.getString((c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_STARTLOC_NAME)));
                    String routeEndLocName = c.getString((c.getColumnIndexOrThrow(RouteEntry.COLUMN_NAME_ENDLOC_NAME)));
                    Location startLoc = new Location("dummyprovider");
                    startLoc.setAltitude(routeStartLocAlt);
                    startLoc.setLongitude(routeStartLocLong);
                    Location endLoc = new Location("dummyprovider");
                    endLoc.setAltitude(routeEndLocAlt);
                    endLoc.setLongitude(routeEndLocLong);
                    routes.add(new Route(routeName, startLoc, endLoc, routeStartLocName, routeEndLocName));
                } while (c.moveToNext());
            }
        }
        c.close();

        return routes;
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
                        RouteEntry.COLUMN_NAME_STARTLOC_ALT + REAL_TYPE +COMMA_SEP +
                        RouteEntry.COLUMN_NAME_STARTLOC_LONG + REAL_TYPE +COMMA_SEP +
                        RouteEntry.COLUMN_NAME_ENDLOC_ALT + REAL_TYPE +COMMA_SEP +
                        RouteEntry.COLUMN_NAME_ENDLOC_LONG + REAL_TYPE +COMMA_SEP +
                        RouteEntry.COLUMN_NAME_STARTLOC_NAME + TEXT_TYPE +COMMA_SEP +
                        RouteEntry.COLUMN_NAME_ENDLOC_NAME + TEXT_TYPE + " )";

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
        private static final String COLUMN_NAME_STARTLOC_ALT = "startlocalt";
        private static final String COLUMN_NAME_STARTLOC_LONG = "startloclong";
        private static final String COLUMN_NAME_ENDLOC_ALT = "endlocalt";
        private static final String COLUMN_NAME_ENDLOC_LONG = "endloclong";
        private static final String COLUMN_NAME_STARTLOC_NAME = "startlocname";
        private static final String COLUMN_NAME_ENDLOC_NAME = "endlocname";
    }
}



