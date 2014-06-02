package com.indoorlocalizer.app.activity.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.indoorlocalizer.app.activity.common.AccessPoint;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by federicostivani on 01/06/14.
 */
public class DbManager{
    private static final String TAG_LOG=DbManager.class.getSimpleName();

    private Context context;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    // Database Version
    private static final String DATABASE_TABLE = "aps";
    // Data structure;
    private static final String KEY_ID = "_id";
    private static final String KEY_SSID = "ssid";
    private static final String KEY_BSSID = "bssid";
    private static final String KEY_CAPABILITIES = "capabilities";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_FREQUENCY = "frequency";


    private static final String[] COLUMNS = {KEY_ID,KEY_SSID,KEY_BSSID,KEY_CAPABILITIES,KEY_LEVEL,KEY_FREQUENCY};

    public DbManager(Context context){
        this.context=context;
    }
    /*
     * Apertura del Database in modalita' RW.
     */
    public DbManager open() throws SQLException {
        dbHelper=new DatabaseHelper(context);
        db=dbHelper.getWritableDatabase();
        return this;
    }
    public void close() {
        dbHelper.close();
    }
    private ContentValues createContentValues(String ssid, String bssid, String capabilities, int level, int frequency){
        ContentValues values=new ContentValues();
        values.put(KEY_SSID,ssid);
        values.put(KEY_BSSID,bssid);
        values.put(KEY_CAPABILITIES,capabilities);
        values.put(KEY_LEVEL,level);
        values.put(KEY_FREQUENCY,frequency);
        return values;
    }

    public long addWifi(AccessPoint ap){
        //for logging
        Log.d("addAccessPoint", ap.toString());
        Log.d("[WRITING AP TO DB]","AP: "+ ap.getSSID()+" "+ap.getBSSID()+" "+ap.getCapabilities()+" "+ap.getLevel()+" "+ap.getFrequency());
        ContentValues value=createContentValues(ap.getSSID(),ap.getBSSID(),ap.getCapabilities(),ap.getLevel(),ap.getFrequency());
        return db.insertOrThrow(DATABASE_TABLE,null,value);
    }
    public Cursor getAllAccessPoints () {
        return db.query(DATABASE_TABLE,COLUMNS,null,null,null,null,null);
    }
    public Cursor getAccessPoint(String  ssid){
        return db.query(DATABASE_TABLE,COLUMNS,KEY_SSID+"="+ssid,null,null,null,null);
    }

}
