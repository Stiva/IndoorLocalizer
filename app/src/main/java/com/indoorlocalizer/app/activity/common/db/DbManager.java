package com.indoorlocalizer.app.activity.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.indoorlocalizer.app.activity.common.model.AccessPoint;
import com.indoorlocalizer.app.activity.common.model.InfrastructureMap;

import java.sql.SQLException;

/**
 * Created by federicostivani on 01/06/14.
 */
public class DbManager{
    private static final String TAG_LOG=DbManager.class.getSimpleName();

    private Context context;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private static final String DATABASE_RP_TABLE = "aps";
    private static final String DATABASE_MAP_TABLE = "maps";
    private String[] COLUMNS_AP = {DatabaseHelper.KEY_MAP_NAME,DatabaseHelper.KEY_ID,DatabaseHelper.KEY_REFERENCE_POINT,DatabaseHelper.KEY_SSID,DatabaseHelper.KEY_BSSID,DatabaseHelper.KEY_CAPABILITIES,DatabaseHelper.KEY_LEVEL,DatabaseHelper.KEY_FREQUENCY,DatabaseHelper.KEY_HITS};
    private String[] COLUMNS_MAP = {DatabaseHelper.KEY_ID,DatabaseHelper.KEY_MAP_NAME,DatabaseHelper.KEY_NUMBER_OF_RP,DatabaseHelper.KEY_MAP_IMAGE_PATH};

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

    private ContentValues createAPContentValues(String map,int rp, String ssid, String bssid, String capabilities, int level, int frequency,int hits){
        ContentValues values=new ContentValues();
        values.put(DatabaseHelper.KEY_MAP_NAME,map);
        values.put(DatabaseHelper.KEY_REFERENCE_POINT,rp);
        values.put(DatabaseHelper.KEY_SSID,ssid);
        values.put(DatabaseHelper.KEY_BSSID,bssid);
        values.put(DatabaseHelper.KEY_CAPABILITIES,capabilities);
        values.put(DatabaseHelper.KEY_LEVEL,level);
        values.put(DatabaseHelper.KEY_FREQUENCY,frequency);
        values.put(DatabaseHelper.KEY_HITS,hits);
        return values;
    }

    private ContentValues createMapContentValues(String mapName,int numberOfRp,String mapImagePath){
        ContentValues values=new ContentValues();
        values.put(DatabaseHelper.KEY_MAP_NAME,mapName);
        values.put(DatabaseHelper.KEY_NUMBER_OF_RP,numberOfRp);
        values.put(DatabaseHelper.KEY_MAP_IMAGE_PATH,mapImagePath);
        return values;
    }

    public long addWifi(AccessPoint ap){
        //for logging
        Log.d("addAccessPoint", ap.toString());
        Log.d("[WRITING AP TO DB]","AP: "+ ap.getSSID()+" "+ap.getBSSID()+" "+ap.getCapabilities()+" "+ap.getLevel()+" "+ap.getFrequency()+" "+ap.getHits());
        ContentValues value=createAPContentValues(ap.getMap(), ap.getRp(), ap.getSSID(), ap.getBSSID(), ap.getCapabilities(), ap.getLevel(), ap.getFrequency(), ap.getHits());
        return db.insertOrThrow(DATABASE_RP_TABLE,null,value);
    }

    public long addMap(InfrastructureMap map){
        long ALREADY_IN_DB = -1;
        if(!checkMapPresence(map.getMapName())) {
            Log.d("Add Map", map.toString());
            Log.d("[WRITING MAP TO DB]","MAP: "+ map.getMapName()+" "+map.getRpNumber());
            ContentValues value=createMapContentValues(map.getMapName(), map.getRpNumber(), map.getMapImagePath());
            return db.insertOrThrow(DATABASE_MAP_TABLE, null, value);
        }
        else {
            updateMapNumberOfRP(map.getMapName());
            return ALREADY_IN_DB;
        }
    }

    public Cursor getAllAccessPoints () {
        return db.query(DATABASE_RP_TABLE, COLUMNS_AP,null,null,null,null,null);
    }

    public Cursor getAccessPoint(String  ssid){
        return db.query(DATABASE_RP_TABLE, COLUMNS_AP,DatabaseHelper.KEY_SSID+"="+ssid,null,null,null,null);
    }

    public Cursor getAccessPointByMap(String map){
        return db.query(DATABASE_RP_TABLE, COLUMNS_AP,DatabaseHelper.KEY_MAP_NAME +"="+map,null,null,null,null);
    }

    public Cursor getMapNameList(){
        return db.query(DATABASE_MAP_TABLE,COLUMNS_MAP,null,null,null,null,null);
    }

    public boolean checkMapPresence(String mapName){
        Cursor mCursor=db.query(DATABASE_MAP_TABLE,COLUMNS_MAP,DatabaseHelper.KEY_MAP_NAME+"= '"+mapName+"'",null,null,null,null);
        return mCursor != null && mCursor.getCount() > 0;
    }

    public void updateMapNumberOfRP(String mapName) {
        if(checkMapPresence(mapName)) {
            ContentValues args = new ContentValues();
            args.put(DatabaseHelper.KEY_NUMBER_OF_RP, getRPNumber(mapName)+1);
            db.update(DATABASE_MAP_TABLE, args, DatabaseHelper.KEY_MAP_NAME + " = '" + mapName+"'", null);
        }
    }

    private int getRPNumber(String mapName) {
        Cursor mCursor=db.query(DATABASE_MAP_TABLE,COLUMNS_MAP,DatabaseHelper.KEY_MAP_NAME +" = '"+mapName+"'",null,null,null,null);
        if(mCursor!=null) {
            mCursor.moveToFirst();
            InfrastructureMap map = new InfrastructureMap(mCursor.getString(0),Integer.parseInt(mCursor.getString(1)),mCursor.getString(1));
            return map.getRpNumber();
        }
        return 0;
    }
}
