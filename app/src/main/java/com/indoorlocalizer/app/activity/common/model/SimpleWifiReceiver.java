package com.indoorlocalizer.app.activity.common.model;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.HashMap;
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
    // Scan wifi at a determined Reference point rp
    public Map<String,AccessPoint> receiveWifi(String map,int rp) {
        List<ScanResult> wifiList = mainWifi.getScanResults();
        for (ScanResult result: wifiList) {
            AccessPoint ap=new AccessPoint(map,rp,result.SSID,result.BSSID,result.capabilities,result.level,result.frequency);
            mModel.put(ap.getSSID(),ap);
        }
        return mModel;
    }
}
