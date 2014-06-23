package com.indoorlocalizer.app.activity.offline;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.db.DbManager;
import com.indoorlocalizer.app.activity.common.model.AccessPoint;
import com.indoorlocalizer.app.activity.common.model.ReferencePoint;
import com.indoorlocalizer.app.activity.common.model.SimpleWifiReceiver;
import com.indoorlocalizer.app.activity.offline.utils.OfflineUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScannerService extends IntentService {
    private int progress;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    //At startup this list is empty, while scanning a reference point it's populated with new read RP every new schedule of the task.
    private Map<String,AccessPoint> referencePoint;
    private String rpName;
    private int rpId;
    private String mapName;
    private String mapImagePath;
    public static boolean finish;
    private DbManager dbManager;
    private ScheduledExecutorService scheduleTaskExecutor;
    private int scanNumber;
    private int durationMS;

    public ScannerService() {
        super("ScannerService");
    }
    @Override
    public void onCreate(){
        finish=false;
        mapImagePath="";
        mBuilder=
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Evaluating a reference point, don't move!");
        mNotificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        progress=-1;
        referencePoint=new HashMap<String, AccessPoint>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        rpName = intent.getExtras().getString("rpName");
        mapName = intent.getExtras().getString("mapName");
        mapImagePath = intent.getExtras().getString("mapImage");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        scanNumber = Integer.parseInt(prefs.getString("scan_number", "0"));
        durationMS = Integer.parseInt(prefs.getString("duration_ms","0"));
        scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
        // This schedule a runnable task every 2 minutes
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if(progress< scanNumber) {
                    progress++;
                    sendNotification();
                    scanWifi();
                } else if (!finish){

                    mergeData();
                    finish=true;
                } else {
                    stopSelf();
                    onDestroy();
                }
            }
        }, 0, durationMS, TimeUnit.MILLISECONDS);
        return IntentService.START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        try{
            scheduleTaskExecutor.shutdown();
            dbManager.close();
        } catch (NullPointerException e) {
            Toast.makeText(this, "Nothing to stop", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    private void sendNotification(){
        mBuilder.setContentText("Progress " + progress + "/"+ scanNumber);
        int mId = 1;
        mNotificationManager.notify(mId, mBuilder.build());
    }

    private void scanWifi(){
        WifiManager mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // Check for wifi is disabled
        if (!mainWifi.isWifiEnabled()) {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();
            mainWifi.setWifiEnabled(true);
        }
        SimpleWifiReceiver actualWifi = new SimpleWifiReceiver(mainWifi);
        rpId=OfflineUtils.getRpNumber(this, mapName);
        //List of scanned wifi
        Map<String,AccessPoint>scannedWifi=actualWifi.receiveWifi(mapName);
        //Compare the new AP read by the scanner, with the previous saved ones
        for(AccessPoint ap:scannedWifi.values()){
            ap.setRp(rpId); //don't forget to update the rp value for the ap.
            if(referencePoint.containsKey(ap.getSSID())){
                referencePoint.get(ap.getSSID()).hit();
                //Updating AP LVL, after finishing this procedure, the level must be updated at avg level (level/hits);
                referencePoint.get(ap.getSSID()).setLevel(ap.getLevel() + referencePoint.get(ap.getSSID()).getLevel());
            } else {
                //New ap to add at fingerprint
                referencePoint.put(ap.getSSID(),ap);
            }
        }
    }

    private void mergeData() {
        for(AccessPoint ap:referencePoint.values()){
            referencePoint.get(ap.getSSID()).setLevel(ap.getLevel()/ap.getHits());
        }
            saveToDb(referencePoint.values());
    }

    private void saveToDb(Collection<AccessPoint> values) {
        dbManager=new DbManager(getApplicationContext());
        try {
            dbManager.open();
            for(AccessPoint ap:values) {
                dbManager.addWifi(ap);
            }
            dbManager.updateMapNumberOfRP(mapName);
            dbManager.addRP(new ReferencePoint(mapName,rpName,rpId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
