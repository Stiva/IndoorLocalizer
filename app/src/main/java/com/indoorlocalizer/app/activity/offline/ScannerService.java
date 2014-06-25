package com.indoorlocalizer.app.activity.offline;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.db.DbManager;
import com.indoorlocalizer.app.activity.common.model.AccessPoint;
import com.indoorlocalizer.app.activity.common.model.ReferencePoint;
import com.indoorlocalizer.app.activity.offline.utils.OfflineUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScannerService extends IntentService {
    public static  boolean finish;
    private boolean finishMerge;
    private int progress;
    private WifiManager mainWifi;
    private List<ScanResult> wifiList;
    private ArrayList<AccessPoint> mModel = new ArrayList<AccessPoint>();
    private ArrayList<AccessPoint> readAps;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    //At startup this list is empty, while scanning a reference point it's populated with new read RP every new schedule of the task.
    private Map<String, AccessPoint> referencePoint;
    private String rpName;
    private int rpId;
    private String mapName;
    private DbManager dbManager;
    private ScheduledExecutorService scheduleTaskExecutor;
    private int scanNumber;
    private BroadcastReceiver mReceiver;
    private int durationMS;
    private int i=0;

    public ScannerService() {
        super("ScannerService");
    }

    @Override
    public void onCreate() {
        finish = false;
        finishMerge=false;
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Evaluating a reference point, don't move!");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        progress = -1;
        referencePoint = new HashMap<String, AccessPoint>();
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        readAps=new ArrayList<AccessPoint>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mReceiver=new WifiReceiver();
        rpName = intent.getExtras().getString("rpName");
        registerReceiver(mReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mapName = intent.getExtras().getString("mapName");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        scanNumber = Integer.parseInt(prefs.getString("scan_number", "0"));
        durationMS = Integer.parseInt(prefs.getString("duration_ms", "0"));
        if (!mainWifi.isWifiEnabled()) {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), getString(R.string.enabling_wifi_message),
                    Toast.LENGTH_LONG).show();
            mainWifi.setWifiEnabled(true);
        }
        scheduleTaskExecutor = Executors.newScheduledThreadPool(scanNumber);
        // This schedule a runnable task every 2 minutes
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (progress < scanNumber) {
                    progress++;
                    sendNotification("Progress " + progress + "/" + scanNumber);
                    mainWifi.startScan();
                } else {
                    unregisterReceiver(mReceiver);
                    mergeData();
                    //scheduleTaskExecutor.shutdown();
                }
            }
        }, 0, durationMS, TimeUnit.MILLISECONDS);
        return IntentService.START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            scheduleTaskExecutor.shutdown();
            dbManager.close();
        } catch (NullPointerException e) {
            Toast.makeText(this, "Nothing to stop", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    private void sendNotification(String text) {
        mBuilder.setContentText(text);
        int mId = 1;
        mNotificationManager.notify(mId, mBuilder.build());
    }

    private void scanWifi() {
        // Check for wifi is disabled
        rpId = OfflineUtils.getRpNumber(this, mapName);
        //List of scanned wifi
        //Compare the new AP read by the scanner, with the previous saved ones
        for (AccessPoint ap:readAps) {
            ap.setRp(rpId);
            ap.setMap(mapName);
            if (referencePoint.containsKey(ap.getSSID())) {
                referencePoint.get(ap.getSSID()).hit();
                //Updating AP LVL, after finishing this procedure, the level must be updated at avg level (level/hits);
                referencePoint.get(ap.getSSID()).setLevel(ap.getLevel() + referencePoint.get(ap.getSSID()).getLevel());
            } else {
                //New ap to add at fingerprint
                referencePoint.put(ap.getSSID(), ap);
            }
        }
    }

    private void mergeData() {
        sendNotification("Merging data");
        for (AccessPoint ap : referencePoint.values()) {
            referencePoint.get(ap.getSSID()).setLevel(ap.getLevel() / ap.getHits());
        }
        saveToDb(referencePoint.values());
    }

    private void saveToDb(Collection<AccessPoint> values) {
        sendNotification("Write to DB");
        dbManager = new DbManager(getApplicationContext());
        try {
            dbManager.open();
            for (AccessPoint ap : values) {
                dbManager.addWifi(ap);
            }
            dbManager.updateMapNumberOfRP(mapName);
            dbManager.addRP(new ReferencePoint(mapName, rpName, rpId));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbManager.close();
        }
        finish=true;
    }
    class WifiReceiver extends BroadcastReceiver {
        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = mainWifi.getScanResults();
            for (ScanResult result : wifiList) {
                final AccessPoint item = new AccessPoint(result.SSID, result.level, result.frequency);
                mModel.add(item);
            }
            readAps=mModel;
            scanWifi();
            mModel.clear();
        }
    }
}
