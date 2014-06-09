package com.indoorlocalizer.app.activity.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.indoorlocalizer.app.activity.common.model.AccessPoint;

import java.sql.SQLException;

/**
 * Created by federicostivani on 01/06/14.
 */
public class DbManager{
    private static final String TAG_LOG=DbManager.class.getSimpleName();

    private Context context;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    private static final String DATABASE_TABLE = "aps";
    private String[] COLUMNS = {DatabaseHelper.KEY_MAP,DatabaseHelper.KEY_ID,DatabaseHelper.KEY_REFERENCE_POINT,DatabaseHelper.KEY_SSID,DatabaseHelper.KEY_BSSID,DatabaseHelper.KEY_CAPABILITIES,DatabaseHelper.KEY_LEVEL,DatabaseHelper.KEY_FREQUENCY,DatabaseHelper.KEY_HITS};

    public DbManager(Context context){
        this.context=context;
    }
    /*
     * Opening DB in RW mode
     */
    public DbManager open() throws SQLException {
        dbHelper=new DatabaseHelper(context);
        db=dbHelper.getWritableDatabase();
        return this;
    }
    public void close() {
        dbHelper.close();
    }
    private ContentValues createContentValues(String map,int rp, String ssid, String bssid, String capabilities, int level, int frequency,int hits){
        ContentValues values=new ContentValues();
        values.put(DatabaseHelper.KEY_MAP,map);
        values.put(DatabaseHelper.KEY_REFERENCE_POINT,rp);
        values.put(DatabaseHelper.KEY_SSID,ssid);
        values.put(DatabaseHelper.KEY_BSSID,bssid);
        values.put(DatabaseHelper.KEY_CAPABILITIES,capabilities);
        values.put(DatabaseHelper.KEY_LEVEL,level);
        values.put(DatabaseHelper.KEY_FREQUENCY,frequency);
        values.put(DatabaseHelper.KEY_HITS,hits);
        return values;
    }

    public long addWifi(AccessPoint ap){
        //for logging
        Log.d("addAccessPoint", ap.toString());
        Log.d("[WRITING AP TO DB]","AP: "+ ap.getSSID()+" "+ap.getBSSID()+" "+ap.getCapabilities()+" "+ap.getLevel()+" "+ap.getFrequency()+" "+ap.getHits());
        ContentValues value=createContentValues(ap.getMap(),ap.getRp(),ap.getSSID(),ap.getBSSID(),ap.getCapabilities(),ap.getLevel(),ap.getFrequency(),ap.getHits());
        return db.insertOrThrow(DATABASE_TABLE,null,value);
    }
    public Cursor getAllAccessPoints () {
        return db.query(DATABASE_TABLE,COLUMNS,null,null,null,null,null);
    }
    public Cursor getAccessPoint(String  ssid){
        return db.query(DATABASE_TABLE,COLUMNS,DatabaseHelper.KEY_SSID+"="+ssid,null,null,null,null);
    }

}
