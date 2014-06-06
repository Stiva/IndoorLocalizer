package com.indoorlocalizer.app.activity.offline;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.model.AccessPoint;
import com.indoorlocalizer.app.activity.common.model.SimpleWifiReceiver;
import com.indoorlocalizer.app.activity.db.DbManager;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScannerService extends IntentService {
    private int progress;
    private int scanNumber = 3;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private Map<String,AccessPoint> referencePoint;
    private int rpId;

    public ScannerService() {
        super("ScannerService");
    }
    @Override
    public void onCreate(){
        mBuilder=
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Evaluating a reference point, don't move!");
        mNotificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        progress=-1;
        referencePoint=new HashMap<String, AccessPoint>();

        // mId allows you to update the notification later on.
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        rpId=intent.getExtras().getInt("rpID");
        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
        // This schedule a runnable task every 2 minutes
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if(progress< scanNumber) {
                    progress++;
                    sendNotification();
                    scanWifi();
                } else {
                    mergeData();
                }
            }
        }, 0, 2, TimeUnit.SECONDS);
        return IntentService.START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        Toast.makeText(this, "Scanning aborted by the user after "+progress+" iteration", Toast.LENGTH_LONG).show();

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
        Map<String,AccessPoint>scannedWifi=actualWifi.receiveWifi(rpId);
        for(AccessPoint ap:scannedWifi.values()){
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
        Toast.makeText(getApplicationContext(),"Data saved to DB",Toast.LENGTH_LONG).show();
        stopSelf();
    }

    private void saveToDb(Collection<AccessPoint> values) {
        DbManager dbManager=new DbManager(getApplicationContext());
        try {
            dbManager.open();
            for(AccessPoint ap:values)
                dbManager.addWifi(ap);
            dbManager.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
