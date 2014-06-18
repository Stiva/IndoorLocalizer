package com.indoorlocalizer.app.activity.offline.utils;

import android.content.Context;

import com.indoorlocalizer.app.activity.common.db.DbManager;

import java.sql.SQLException;

public class OfflineUtils {
    public static int getRpNumber(Context c, String mapName) {
        DbManager dbManager=new DbManager(c.getApplicationContext());
        int res=0;
        try {
            dbManager.open();
            res=dbManager.getLastRp(mapName)+1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static int getMapRpNumber(Context c,String mapName) {
        DbManager dbManager=new DbManager(c.getApplicationContext());
        int res=1;
        try {
            dbManager.open();
            res=dbManager.getRPNumber(mapName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
}
