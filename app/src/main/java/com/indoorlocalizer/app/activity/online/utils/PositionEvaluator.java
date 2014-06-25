package com.indoorlocalizer.app.activity.online.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.common.db.DbManager;
import com.indoorlocalizer.app.activity.common.model.AccessPoint;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PositionEvaluator extends Activity {
    private WifiManager mainWifi;
    private Cursor mCursor;
    private DbManager dbManager;
    private ProgressDialog barProgressDialog;
    private String mapName;
    private List<ScanResult> wifiList;
    private ArrayList<AccessPoint> mModel = new ArrayList<AccessPoint>();
    private ArrayList<AccessPoint> readAps;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private TextView rpNumber;
    private TextView rpName;
    private TextView unableFindMessage;
    private Button reScanButton;
    private BroadcastReceiver mReceiver;
    private String foundRpName;
    private int foundRpId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_evaluator);
        barProgressDialog = new ProgressDialog(this);
        barProgressDialog.setProgress(0);
        Intent intent = getIntent();
        mapName = intent.getExtras().getString("mapName");
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Localizing...");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        wifiList = new ArrayList<ScanResult>();
        //Read AP from database:
        dbManager = new DbManager(getApplicationContext());
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        rpNumber = (TextView) findViewById(R.id.localized_rp_number);
        rpName = (TextView) findViewById(R.id.localized_rp_name);
        unableFindMessage = (TextView) findViewById(R.id.localized_unable_find);
        reScanButton = (Button) findViewById(R.id.localized_scan_button);
        reScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchBarDialog();
                mainWifi.startScan();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        launchBarDialog();
        sendNotification(getString(R.string.evaluating_euclidean_distance_message));
        // Check for wifi is disabled
        if (!mainWifi.isWifiEnabled()) {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), getString(R.string.enabling_wifi_message),
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }
        try {
            IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mReceiver = new WifiReceiver();
            registerReceiver(mReceiver, filter);

        } catch (Exception e) {
            e.printStackTrace();
        }
        mainWifi.startScan();
        //List of AP received in current position
    }

    public void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    public void onResume() {
        registerReceiver(mReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    private void sendNotification(String text) {
        mBuilder.setContentText(text);
        int mId = 1;
        mNotificationManager.notify(mId, mBuilder.build());
    }

    private boolean compareRP() {
        SparseArray<ArrayList<AccessPoint>> map = new SparseArray<ArrayList<AccessPoint>>();
        SparseArray<ArrayList<Double>> differences = new SparseArray<ArrayList<Double>>();
        ArrayList<Integer> ids = new ArrayList<Integer>();
        try {
            dbManager.open();
            //Number of RP for a specified map
            int rpNumber = dbManager.getRPNumber(mapName);
            for (int i = 1; i <= rpNumber; i++) {
                mCursor = dbManager.getAccessPointByMapAndRP(mapName, i);
                ArrayList<AccessPoint> aps = getAParray(mCursor);
                ArrayList<AccessPoint> values = new ArrayList<AccessPoint>();
                int rpId = 0;
                for (AccessPoint ap : aps) {
                    values.add(ap);
                    rpId = ap.getRp();
                }
                map.put(rpId, values);
                ids.add(rpId);
            }
            for (int i = 0; i < map.size(); i++) {
                ArrayList<Double> difference = new ArrayList<Double>();
                for (AccessPoint readAP : readAps) {
                    for (AccessPoint aSavedAP : map.get(ids.get(i))) {
                        if (readAP.getSSID().equals(aSavedAP.getSSID())) {
                            Double tmp = EuclideanDifference2(readAP, aSavedAP);
                            difference.add(tmp);
                        }
                    }
                }
                differences.put(ids.get(i), difference);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mCursor.close();
            dbManager.close();
        }
        return searchMinimumArray(differences, ids);
    }

    private boolean searchMinimumArray(SparseArray<ArrayList<Double>> map, ArrayList<Integer> ids) {
        double min = 1000000000;
        String rpMin = "";
        int rpIdMin = -1;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        double tolerance = Double.parseDouble(prefs.getString("tolerance", "1.0"));
        for (int i = 0; i < map.size(); i++) {
            double sum = 0;
            for (Double value : map.get(ids.get(i))) {
                sum += value;
            }
            if (sum / map.size() < min && sum != 0) {
                min = sum / map.get(ids.get(i)).size();
                try {
                    dbManager.open();
                    rpMin = dbManager.getRpName(mapName, ids.get(i));
                    rpIdMin = ids.get(i);
                    //dbManager.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    dbManager.close();
                }
            }
        }
        if (min < tolerance) {
            foundRpName = rpMin;
            foundRpId = rpIdMin;
            return true;
        } else
            return false;
    }

    private ArrayList<AccessPoint> getAParray(Cursor mCursor) {
        ArrayList<AccessPoint> result = new ArrayList<AccessPoint>();
        while (mCursor.moveToNext()) {
            result.add(new AccessPoint(mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_NAME)),
                    mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_REFERENCE_POINT_ID)),
                    mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SSID)),
                    mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BSSID)),
                    mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CAPABILITIES)),
                    mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LEVEL)),
                    mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_FREQUENCY))));
        }
        mCursor.moveToFirst();
        return result;
    }

    /**
     * @param a First access point
     * @param b Second access point
     * @return true if the signal difference is less or equal then the admitted tolerance, false otherwise. (Tolerance could be substituted with variance, estimated in offline phase)
     */
    private double EuclideanDifference2(AccessPoint a, AccessPoint b) {
        double difference;
        difference = Math.abs(a.getLevel() * a.getLevel() - b.getLevel() * b.getLevel());
        difference = Math.sqrt(difference);
        return difference;
    }

    private void evaluate() {
        boolean result = compareRP();

        if (result) {
            rpName.setVisibility(View.VISIBLE);
            rpNumber.setVisibility(View.VISIBLE);
            unableFindMessage.setVisibility(View.INVISIBLE);
            sendNotification(getString(R.string.notification_successful_localization, foundRpName, foundRpId));
            rpName.setText(getResources().getString(R.string.found_rp_name_value_pattern, foundRpName));
            rpNumber.setText(getResources().getString(R.string.found_rp_value_pattern, foundRpId));
        } else {
            rpName.setVisibility(View.INVISIBLE);
            rpNumber.setVisibility(View.INVISIBLE);
            unableFindMessage.setVisibility(View.VISIBLE);
            sendNotification(getString(R.string.unable_localize_message));
        }
        barProgressDialog.dismiss();
    }

    public void launchBarDialog() {
        barProgressDialog.setTitle(getString(R.string.localizing_dialog_title,mapName));
        barProgressDialog.setMessage(getString(R.string.dialog_scan_map_message));
        barProgressDialog.setProgressStyle(ProgressDialog.THEME_HOLO_LIGHT);
        barProgressDialog.setCancelable(false);
        barProgressDialog.show();
    }

    class WifiReceiver extends BroadcastReceiver {
        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = mainWifi.getScanResults();
            for (ScanResult result : wifiList) {
                AccessPoint item = new AccessPoint(result.SSID, result.level, result.frequency);
                mModel.add(item);
            }
            readAps = mModel;
            evaluate();
            mModel.clear();
        }
    }

}
