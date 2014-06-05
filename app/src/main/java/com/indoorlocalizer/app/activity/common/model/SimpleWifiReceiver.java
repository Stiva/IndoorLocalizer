package com.indoorlocalizer.app.activity.common.model;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by federicostivani on 05/06/14.
 */
public class SimpleWifiReceiver{
    private WifiManager mainWifi;
    private Map<String,AccessPoint> mModel = new HashMap<String,AccessPoint>();
    public SimpleWifiReceiver(WifiManager wifiMng){
        this.mainWifi=wifiMng;
    }
    // This method call when number of wifi connections changed
    public Map<String,AccessPoint> receiveWifi() {
        List<ScanResult> wifiList = mainWifi.getScanResults();
        for (ScanResult result: wifiList) {
            AccessPoint ap=new AccessPoint(result.SSID,result.BSSID,result.capabilities,result.level,result.frequency);
            Map<String,AccessPoint> tempMap=new HashMap<String, AccessPoint>();
            mModel.put(ap.getSSID(),ap);
        }
        return mModel;
    }
}
