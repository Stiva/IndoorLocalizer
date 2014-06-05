package com.indoorlocalizer.app.activity.db.model;

import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteQuery;

import com.indoorlocalizer.app.activity.common.model.AccessPoint;
import com.indoorlocalizer.app.activity.db.DatabaseHelper;

/**
 * Created by federicostivani on 03/06/14.
 */
public class LocalDataCursor extends SQLiteCursor {


    public LocalDataCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        super(driver, editTable, query);
    }

    public long getId() {
        return getLong(getColumnIndex(DatabaseHelper.KEY_ID));
    }
    public String getSsid() {
        return getString(getColumnIndex(DatabaseHelper.KEY_SSID));
    }
    public String getBssid() {
        return getString(getColumnIndex(DatabaseHelper.KEY_BSSID));
    }
    public String getCapabilities() {
        return getString(getColumnIndex(DatabaseHelper.KEY_CAPABILITIES));
    }
    public int getLevel() {
        return getInt(getColumnIndex(DatabaseHelper.KEY_LEVEL));
    }
    public int getFrequency() {
        return getInt(getColumnIndex(DatabaseHelper.KEY_FREQUENCY));
    }
    public AccessPoint asAccessPoint(){
        return new AccessPoint(getSsid(),getBssid(),getCapabilities(),getLevel(),getFrequency());
    }

}
