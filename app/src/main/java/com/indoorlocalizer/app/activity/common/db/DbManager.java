package com.indoorlocalizer.app.activity.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.indoorlocalizer.app.activity.common.model.AccessPoint;
import com.indoorlocalizer.app.activity.common.model.InfrastructureMap;
import com.indoorlocalizer.app.activity.common.model.ReferencePoint;

import java.sql.SQLException;

public class DbManager{
    private static final String TAG_LOG=DbManager.class.getSimpleName();

    private Context context;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private static final String DATABASE_AP_TABLE = "aps";
    private static final String DATABASE_MAP_TABLE = "maps";
    private static final String DATABASE_RP_TABLE = "rps";
    private String[] COLUMNS_AP = {DatabaseHelper.KEY_MAP_NAME,DatabaseHelper.KEY_ID,DatabaseHelper.KEY_REFERENCE_POINT_ID,DatabaseHelper.KEY_SSID,DatabaseHelper.KEY_BSSID,DatabaseHelper.KEY_CAPABILITIES,DatabaseHelper.KEY_LEVEL,DatabaseHelper.KEY_FREQUENCY,DatabaseHelper.KEY_HITS};
    private String[] COLUMNS_MAP = {DatabaseHelper.KEY_ID,DatabaseHelper.KEY_MAP_NAME,DatabaseHelper.KEY_NUMBER_OF_RP,DatabaseHelper.KEY_MAP_IMAGE_PATH};
    private String[] COLUMNS_RP = {DatabaseHelper.KEY_ID,DatabaseHelper.KEY_MAP_NAME,DatabaseHelper.KEY_REFERENCE_POINT_NAME,DatabaseHelper.KEY_REFERENCE_POINT_ID};

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
        values.put(DatabaseHelper.KEY_REFERENCE_POINT_ID,rp);
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

    private ContentValues createRPContentValues(String mapName,String name,int id){
        ContentValues values=new ContentValues();
        values.put(DatabaseHelper.KEY_MAP_NAME,mapName);
        values.put(DatabaseHelper.KEY_REFERENCE_POINT_NAME,name);
        values.put(DatabaseHelper.KEY_REFERENCE_POINT_ID,id);
        return values;
    }

    public long addWifi(AccessPoint ap){
        //for logging
        Log.d("addAccessPoint", ap.toString());
        Log.d("[WRITING AP TO DB]","AP: "+ ap.getSSID()+" "+ap.getBSSID()+" "+ap.getCapabilities()+" "+ap.getLevel()+" "+ap.getFrequency()+" "+ap.getHits());
        ContentValues value=createAPContentValues(ap.getMap(), ap.getRp(), ap.getSSID(), ap.getBSSID(), ap.getCapabilities(), ap.getLevel(), ap.getFrequency(), ap.getHits());
        return db.insertOrThrow(DATABASE_AP_TABLE,null,value);
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

    public long addRP (ReferencePoint rp){
        long ALREADY_IN_DB=-1;
        if(!checkRpPresence(rp.getName())){
            Log.d("Add RP",rp.toString());
            Log.d("[WRITING RP TO DB]","RP: "+rp.getName() +" "+rp.getId()+" in map: "+rp.getMapName());
            ContentValues value=createRPContentValues(rp.getMapName(),rp.getName(),rp.getId());
            return db.insertOrThrow(DATABASE_RP_TABLE,null,value);
        }
        return ALREADY_IN_DB;
    }

    public Cursor getAccessPointByMap(String map){
        return db.query(DATABASE_AP_TABLE, COLUMNS_AP,DatabaseHelper.KEY_MAP_NAME +"= '"+map+"'",null,null,null,null);
    }

    public Cursor getMapNameList(){
        return db.query(DATABASE_MAP_TABLE,COLUMNS_MAP,null,null,null,null,null);
    }

    public boolean checkMapPresence(String mapName){
        Cursor mCursor = db.query(DATABASE_MAP_TABLE,COLUMNS_MAP,DatabaseHelper.KEY_MAP_NAME+"= '"+mapName+"'",null,null,null,null);
        return mCursor != null && mCursor.getCount() > 0;
    }

    public boolean checkRpPresence(String rpName){
        Cursor mCursor = db.query(DATABASE_RP_TABLE,COLUMNS_RP,DatabaseHelper.KEY_REFERENCE_POINT_NAME+"= '"+rpName+"'",null,null,null,null);
        return mCursor != null && mCursor.getCount() > 0;
    }


    public void updateMapNumberOfRP(String mapName) {
        if(checkMapPresence(mapName)) {
            ContentValues args = new ContentValues();
            args.put(DatabaseHelper.KEY_NUMBER_OF_RP, getRPNumber(mapName)+1);
            db.update(DATABASE_MAP_TABLE, args, DatabaseHelper.KEY_MAP_NAME + " = '" + mapName+"'", null);
        }
    }

    public int getRPNumber(String mapName) {
        Cursor mCursor = db.query(DATABASE_MAP_TABLE,COLUMNS_MAP,DatabaseHelper.KEY_MAP_NAME +" = '"+mapName+"'",null,null,null,null);
        InfrastructureMap map = new InfrastructureMap();
        while(mCursor.moveToNext()) {
             map = new InfrastructureMap(mCursor.getString(   mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_NAME)),
                                                              mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_NUMBER_OF_RP)),
                                                              mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_IMAGE_PATH)));
        }
        return map.getRpNumber();
    }

    public Cursor getAccessPointByMapAndRP(String mapName, int rpIndex){
        String query=(  "SELECT * FROM "+ DATABASE_AP_TABLE +" WHERE "
                        +DatabaseHelper.KEY_MAP_NAME+" = '"+mapName+"' AND "
                        +DatabaseHelper.KEY_REFERENCE_POINT_ID +" = "+rpIndex);
        return db.rawQuery(query,null);
    }

    public void deleteMapByName(String mapName) {
        String s="DELETE FROM "+ DATABASE_MAP_TABLE+" WHERE "+DatabaseHelper.KEY_MAP_NAME+" = '"+mapName+"'";
        db.execSQL(s);
    }

    public void deleteApByMapName(String mapName) {
        String s="DELETE FROM "+ DATABASE_AP_TABLE +" WHERE "+DatabaseHelper.KEY_MAP_NAME+" = '"+mapName+"'";
        db.execSQL(s);
    }

    public void deleteRpByMapName(String mapName) {
        String s="DELETE FROM "+ DATABASE_RP_TABLE +" WHERE "+DatabaseHelper.KEY_MAP_NAME+" = '"+mapName+"'";
        db.execSQL(s);
    }

    public int getLastRp(String mapName) {
        String s="SELECT MAX("+DatabaseHelper.KEY_REFERENCE_POINT_ID+") FROM "+DATABASE_RP_TABLE+" WHERE "+DatabaseHelper.KEY_MAP_NAME+" = '"+mapName+"'";
        Cursor c=db.rawQuery(s,null);
        int res=0;
        while (c.moveToNext()){
            res=c.getInt(0);
        }
        return res;
    }

    public String getRpName(Integer rp) {
        String res="";
        String query="SELECT "+DatabaseHelper.KEY_REFERENCE_POINT_NAME+" FROM "+DATABASE_RP_TABLE+" WHERE "+DatabaseHelper.KEY_REFERENCE_POINT_ID+" ="+rp;
        Cursor mCursor=db.rawQuery(query,null);
        while (mCursor.moveToNext()){
            res=mCursor.getString(0);
        }
        return res;
    }

    public String getImagePath(String mapName) {
        String res="";
        String query="SELECT "+DatabaseHelper.KEY_MAP_IMAGE_PATH+" FROM "+DATABASE_MAP_TABLE+" WHERE "+DatabaseHelper.KEY_MAP_NAME+" = '"+mapName+"'";
        Cursor mCursor=db.rawQuery(query,null);
        while (mCursor.moveToNext()){
            res=mCursor.getString(0);
        }
        return res;
    }
}
