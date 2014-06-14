package com.indoorlocalizer.app.activity.common.db.model;

import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteQuery;

import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.common.model.AccessPoint;

/**
 * Created by federicostivani on 03/06/14.
 */
public class LocalDataCursor extends SQLiteCursor {


    public LocalDataCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        super(driver, editTable, query);
    }
    public String getMap() { return  getString(getColumnIndex(DatabaseHelper.KEY_MAP_NAME));}
    public long getId() {
        return getLong(getColumnIndex(DatabaseHelper.KEY_ID));
    }
    public String getSsid() {
        return getString(getColumnIndex(DatabaseHelper.KEY_SSID));
    }
    public int getRpId() { return getInt(getColumnIndex(DatabaseHelper.KEY_REFERENCE_POINT));}
    public String getBssid() {
        return getString(getColumnIndex(DatabaseHelper.KEY_BSSID));
    }
    public String getCapabilities() { return getString(getColumnIndex(DatabaseHelper.KEY_CAPABILITIES)); }
    public int getLevel() {
        return getInt(getColumnIndex(DatabaseHelper.KEY_LEVEL));
    }
    public int getFrequency() {
        return getInt(getColumnIndex(DatabaseHelper.KEY_FREQUENCY));
    }
    public AccessPoint asAccessPoint(){
        return new AccessPoint(getMap(),getRpId(),getSsid(),getBssid(),getCapabilities(),getLevel(),getFrequency());
    }
}
