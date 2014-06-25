package com.indoorlocalizer.app.activity.offline.utils;

import android.content.Context;
import android.net.wifi.ScanResult;

import com.indoorlocalizer.app.activity.common.db.DbManager;
import com.indoorlocalizer.app.activity.common.model.AccessPoint;
import com.indoorlocalizer.app.activity.common.model.InfrastructureMap;
import com.indoorlocalizer.app.activity.common.utils.CommonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

public class OfflineUtils {

    public static int getRpNumber(Context c, String mapName) {
        DbManager dbManager = new DbManager(c.getApplicationContext());
        int res = 0;
        try {
            dbManager.open();
            res = dbManager.getLastRp(mapName) + 1;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbManager.close();
        }
        return res;
    }

    public static void saveFingerprint(Context c, String mapName, List<ScanResult> wifiList) {
        DbManager dbManager = new DbManager(c.getApplicationContext());
        try {
            dbManager.open();
            int rp = OfflineUtils.getRpNumber(c.getApplicationContext(), mapName);
            for (ScanResult res : wifiList) {
                dbManager.addWifi(new AccessPoint(mapName, rp, res.SSID, res.BSSID, res.capabilities, res.level, res.frequency));
            }
            InputStream src;
            String imageFilePath = "map_default_icon.png";
            if (imageFilePath.equals("map_default_icon.png")) {
                src = c.getAssets().open(imageFilePath);
            } else {
                src = new FileInputStream(imageFilePath);
            }
            File dest = new File(c.getApplicationContext().getFilesDir(), mapName);
            CommonUtils.copy(src, dest);
            dbManager.addMap(new InfrastructureMap(mapName, 1, dest.getPath()));
            dbManager.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void insertNewMap(Context c, String mapName, String imageFilePath) {
        try {
            InputStream src;
            if (imageFilePath.equals("map_default_icon.png")) {
                src = c.getAssets().open(imageFilePath);
            } else {
                src = new FileInputStream(imageFilePath);
            }
            File dest = new File(c.getApplicationContext().getFilesDir(), mapName);
            CommonUtils.copy(src, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DbManager dbManager = new DbManager(c.getApplicationContext());
        try {
            dbManager.open();
            InputStream src;
            if (imageFilePath.equals("map_default_icon.png")) {
                src = c.getAssets().open(imageFilePath);
            } else {
                src = new FileInputStream(imageFilePath);
            }
            File dest = new File(c.getApplicationContext().getFilesDir(), mapName);
            CommonUtils.copy(src, dest);
            dbManager.addMap(new InfrastructureMap(mapName, 0, dest.getPath()));
            dbManager.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dbManager.close();
        }
    }
}
