package com.indoorlocalizer.app.activity.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by federicostivani on 01/06/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME ="access_points.db";
    private static final int DB_VERSION = 2;
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DATABASE CREATION","Creating database "+DB_NAME);
        // SQL statement to create book table
        String CREATE_APS_TABLE = "CREATE TABLE aps ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "ssid TEXT, " +
                "bssid TEXT, "+
                "capabilities TEXT, "+
                "level INTEGER, "+
                "frequency INTEGER )";

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
