package com.indoorlocalizer.app.activity.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database helper. Create tables, and set up the connection to SQLite.
 * If there are updates in the DB structure don't forget to update DB_VERSION to auto update tables!
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "access_points.db";
    public static final String KEY_ID = "_id";
    public static final String KEY_MAP_NAME = "map_name";
    public static final String KEY_REFERENCE_POINT_ID = "reference_point";
    public static final String KEY_REFERENCE_POINT_NAME = "rp_name";
    public static final String KEY_SSID = "ssid";
    public static final String KEY_BSSID = "bssid";
    public static final String KEY_CAPABILITIES = "capabilities";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_FREQUENCY = "frequency";
    public static final String KEY_HITS = "hits";
    public static final String KEY_NUMBER_OF_RP = "reference_point_number";
    public static final String KEY_MAP_IMAGE_PATH = "map_image_path";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DATABASE CREATION", "Creating database " + DB_NAME);
        // SQL statement to create APs table
        String CREATE_APS_TABLE = "CREATE TABLE aps ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "map_name TEXT, " +
                "reference_point INTEGER," +
                "ssid TEXT, " +
                "bssid TEXT, " +
                "capabilities TEXT, " +
                "level INTEGER, " +
                "frequency INTEGER, " +
                "hits INTEGER )";
        // create ap table
        db.execSQL(CREATE_APS_TABLE);
        String CREATE_MAPS_TABLE = "CREATE TABLE maps ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "map_name TEXT, " +
                "reference_point_number INTEGER," +
                "map_image_path TEXT )";
        db.execSQL(CREATE_MAPS_TABLE);
        //Create rps table
        String CREATE_RPS_TABLE = "CREATE TABLE rps ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "map_name TEXT, " +
                "rp_name TEXT," +
                "reference_point INTEGER)";
        db.execSQL(CREATE_RPS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older aps table if existed
        db.execSQL("DROP TABLE IF EXISTS aps");
        db.execSQL("DROP TABLE IF EXISTS maps");
        db.execSQL("DROP TABLE IF EXISTS rps");
        // create fresh aps table
        this.onCreate(db);
    }
}
