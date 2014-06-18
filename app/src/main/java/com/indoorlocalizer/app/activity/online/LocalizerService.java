package com.indoorlocalizer.app.activity.online;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.common.db.DbManager;
import com.indoorlocalizer.app.activity.common.model.AccessPoint;
import com.indoorlocalizer.app.activity.common.utils.CommonUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalizerService extends IntentService {

    private WifiManager mainWifi;
    private Cursor mCursor;
    private DbManager dbManager;
    private String mapName;
    private ArrayList<AccessPoint> readAps;
    public LocalizerService (){super("LocalizerService");}

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    @Override
    protected void onHandleIntent(Intent intent) {

    }

    public void onCreate(){
        /*
         * Notification
         */
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Localizing...");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        readAps =new ArrayList<AccessPoint>();
        //Read AP from database:
        dbManager=new DbManager(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mapName = intent.getExtras().getString("mapName");
        sendNotification();
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // Check for wifi is disabled
        if (!mainWifi.isWifiEnabled()) {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }
        mainWifi.startScan();
        List<ScanResult> scanResults=mainWifi.getScanResults();
        readAps=getAps(scanResults);
        //List of AP received in current position
        return compareRP();
    }

    private ArrayList<AccessPoint> getAps(List<ScanResult> scanResults) {
        ArrayList<AccessPoint> temp = new ArrayList<AccessPoint>();
        for(ScanResult s:scanResults){
            temp.add(new AccessPoint(s.SSID,s.level,s.frequency));
        }
        return temp;
    }

    private void sendNotification(){
        mBuilder.setContentText("evaluating euclidean difference");
        int mId = 1;
        mNotificationManager.notify(mId, mBuilder.build());
    }
    /**
     *
     * @return -1 if the selected RP isn't in the current map, RP number otherwise
     */
    private int compareRP(){
        //1- Read the number of RP of a single map
        int rpNumber=-1;
        HashMap<AccessPoint[],Boolean> responseHashMap = new HashMap<AccessPoint[], Boolean>();
        ArrayList<AccessPoint> selectedAP=new ArrayList<AccessPoint>();
        try {
            dbManager.open();
            rpNumber = dbManager.getRPNumber(mapName);
            //dbManager.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Scan all RP of a specified map
        for(int i=1;i<=rpNumber;i++) {
            try {
                dbManager.open();
                mCursor = dbManager.getAccessPointByMapAndRP(mapName, i);
                //dbManager.close();
                selectedAP = getAParray(mCursor);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //Compare each RP with the mobile user detection.
            for (AccessPoint aReadAp : readAps) {
                //Obtaining the right AP array associated to a specific RP
                for (AccessPoint aSelectedAP : selectedAP) {
                    if(EuclideanDifference(aReadAp, aSelectedAP, CommonUtils.tolerance)){
                        responseHashMap.put(new AccessPoint[]{aReadAp,aSelectedAP},true);
                    } else
                        responseHashMap.put(new AccessPoint[]{aReadAp,aSelectedAP},false);
                }
            }
        }
        return -1;
    }

    /*private HashMap<Integer, ArrayList<AccessPoint>> getMap(Cursor accessPointByMap) {
        HashMap<Integer,ArrayList<AccessPoint>> temp = new HashMap<Integer, ArrayList<AccessPoint>>();
        ArrayList<AccessPoint> apList = new ArrayList<AccessPoint>();
        String ssid;
        int rp,level,frequency;
        while(accessPointByMap.moveToNext()){
            rp=accessPointByMap.getInt(accessPointByMap.getColumnIndexOrThrow(DatabaseHelper.KEY_REFERENCE_POINT));
            ssid=accessPointByMap.getString(accessPointByMap.getColumnIndexOrThrow(DatabaseHelper.KEY_SSID));
            level=accessPointByMap.getInt(accessPointByMap.getColumnIndexOrThrow(DatabaseHelper.KEY_LEVEL));
            frequency=accessPointByMap.getInt(accessPointByMap.getColumnIndexOrThrow(DatabaseHelper.KEY_FREQUENCY));
            apList.add(new AccessPoint(rp,ssid,level,frequency));
        }
        return temp;
    }*/

    private ArrayList<AccessPoint> getAParray(Cursor mCursor) {
        ArrayList<AccessPoint> result=new ArrayList<AccessPoint>();
        while (mCursor.moveToNext()) {
            result.add(new AccessPoint(
                    //mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_REFERENCE_POINT)),
                    mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SSID)),
                    mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LEVEL)),
                    mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_FREQUENCY))));
        }
        mCursor.moveToFirst();
        return result;
    }

    /**
     *
     * @param a First access point
     * @param b Second access point
     * @param tolerance acceptable difference between a.
     * @return true if the signal difference is less or equal then the admitted tolerance, false otherwise. (Tolerance could be substituted with variance, estimated in offline phase)
     */
    private boolean EuclideanDifference(AccessPoint a,AccessPoint b,double tolerance){
        return Math.abs(a.getLevel() * a.getLevel() - b.getLevel() * b.getLevel()) <= tolerance;
    }
}
