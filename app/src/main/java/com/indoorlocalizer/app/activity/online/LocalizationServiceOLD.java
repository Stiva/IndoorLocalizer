package com.indoorlocalizer.app.activity.online;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.SparseArray;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.common.db.DbManager;
import com.indoorlocalizer.app.activity.common.model.AccessPoint;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LocalizationServiceOLD extends IntentService {

    private WifiManager mainWifi;
    private Cursor mCursor;
    private DbManager dbManager;
    private String mapName;
    private List<ScanResult> wifiList;
    private ArrayList<AccessPoint> mModel = new ArrayList<AccessPoint>();
    private ArrayList<AccessPoint> readAps;

    public LocalizationServiceOLD(){super("LocalizerService");}

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
        wifiList =new ArrayList<ScanResult>();
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
        try {
            IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            BroadcastReceiver mReceiver = new WifiReceiver();
            registerReceiver(mReceiver, filter);

        } catch (Exception e) {
            e.printStackTrace();
        }
        mainWifi.startScan();
        readAps=getAps(mainWifi.getScanResults());
        //List of AP received in current position
        String result=compareRP();
        if(!result.isEmpty()) {
            Toast.makeText(this, "Localized in RP: " + result, Toast.LENGTH_LONG).show();
            stopSelf();
        }
        else {
            Toast.makeText(this, "Unable to localize you in this map", Toast.LENGTH_LONG).show();
            stopSelf();
        }
        return START_NOT_STICKY;
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
    private String compareRP(){
        SparseArray<ArrayList<AccessPoint>> map = new SparseArray<ArrayList<AccessPoint>>();
        SparseArray<ArrayList<Double>> differences = new SparseArray<ArrayList<Double>>();
        ArrayList<Integer> ids=new ArrayList<Integer>();
        try{
            dbManager.open();
            int rpNumber=dbManager.getRPNumber(mapName);
            for(int i=1;i<=rpNumber;i++){
                mCursor=dbManager.getAccessPointByMapAndRP(mapName,i);
                ArrayList<AccessPoint> aps=getAParray(mCursor);
                ArrayList<AccessPoint>values = new ArrayList<AccessPoint>();
                int rpId=0;
                for(AccessPoint ap:aps){
                    values.add(ap);
                    rpId=ap.getRp();
                }
                map.put(rpId,values);
                ids.add(rpId);
            }
            for(int i=0;i<map.size();i++){
                ArrayList<Double> difference = new ArrayList<Double>();
                for(AccessPoint readAP:readAps){
                    for(AccessPoint aSavedAP:map.get(ids.get(i))) {
                        if (readAP.getSSID().equals(aSavedAP.getSSID())){
                            Double tmp=EuclideanDifference2(readAP,aSavedAP);
                            difference.add(tmp);
                        }
                    }
                }
                differences.put(ids.get(i),difference);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            mCursor.close();
        }
        return searchMinimumArray(differences,ids);
    }
    private String searchMinimumArray(SparseArray<ArrayList<Double>> map,ArrayList<Integer>ids) {
        double min=1000000000;
        String rpMin="";
        int rpIdMin=-1;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        double tolerance=Double.parseDouble(prefs.getString("tolerance", "1.0"));
        for(int i=0;i<map.size();i++){
            double sum=0;
            for(Double value:map.get(ids.get(i))){
                sum+=value;
            }
            if(sum/map.size()<min && sum!=0){
                min=sum/map.size();
                try {
                    dbManager.open();
                    rpMin=dbManager.getRpName(mapName, ids.get(i));
                    rpIdMin=ids.get(i);
                    //dbManager.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        if(min<tolerance)
            return rpMin+ " nel RP "+ rpIdMin;
        else
            return "";
    }

    private ArrayList<AccessPoint> getAParray(Cursor mCursor) {
        ArrayList<AccessPoint> result=new ArrayList<AccessPoint>();
        while (mCursor.moveToNext()) {
            result.add(new AccessPoint( mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_NAME)),
                                        mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_REFERENCE_POINT_ID)),
                                        mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SSID)),
                                        mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BSSID)),
                                        mCursor.getString(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CAPABILITIES)),
                                        mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LEVEL)),
                                        mCursor.getInt(mCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_FREQUENCY)))) ;
        }
        mCursor.moveToFirst();
        return result;
    }

    /**
     *
     * @param a First access point
     * @param b Second access point
     * @return true if the signal difference is less or equal then the admitted tolerance, false otherwise. (Tolerance could be substituted with variance, estimated in offline phase)
     */
    private double EuclideanDifference2(AccessPoint a,AccessPoint b){
        double difference;
        difference=Math.abs(a.getLevel() * a.getLevel() - b.getLevel() * b.getLevel());
        difference=Math.sqrt(difference);
        return difference;
    }
    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = mainWifi.getScanResults();
            for (ScanResult result:wifiList) {
                final AccessPoint item = new AccessPoint(result.SSID,result.level,result.frequency);
                mModel.add(item);
            }
            readAps=mModel;
            mModel.clear();
        }
    }
}
