package com.indoorlocalizer.app.activity.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by federicostivani on 01/06/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME ="access_points.db";
    public static final String KEY_ID = "_id";
    public static final String KEY_REFERENCE_POINT="reference_point";
    public static final String KEY_SSID = "ssid";
    public static final String KEY_BSSID = "bssid";
    public static final String KEY_CAPABILITIES = "capabilities";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_FREQUENCY = "frequency";
    public static final String KEY_HITS="hits";

    private static final int DB_VERSION = 1;
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DATABASE CREATION","Creating database "+DB_NAME);
        // SQL statement to create APs table
        String CREATE_APS_TABLE = "CREATE TABLE aps ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reference_point INTEGER,"+
                "ssid TEXT, " +
                "bssid TEXT, "+
                "capabilities TEXT, "+
                "level INTEGER, "+
                "frequency INTEGER, " +
                "hits INTEGER )";

        // create ap table
        db.execSQL(CREATE_APS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older aps table if existed
        db.execSQL("DROP TABLE IF EXISTS aps");
        // create fresh aps table
        this.onCreate(db);
    }
}
