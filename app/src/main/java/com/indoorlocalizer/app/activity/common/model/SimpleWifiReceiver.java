package com.indoorlocalizer.app.activity.common.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.SimpleAdapter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by federicostivani on 05/06/14.
 */
public class SimpleWifiReceiver{
    private WifiManager mainWifi;
    private LinkedList<AccessPoint> mModel = new LinkedList<AccessPoint>();
    public SimpleWifiReceiver(WifiManager wifiMng){
        this.mainWifi=wifiMng;
    }
    // This method call when number of wifi connections changed
    public LinkedList<AccessPoint> receiveWifi() {
        List<ScanResult> wifiList = mainWifi.getScanResults();
        for (ScanResult result: wifiList) {
            final AccessPoint ap=new AccessPoint(result.SSID,result.BSSID,result.capabilities,result.level,result.frequency);
            mModel.add(ap);
        }
        return mModel;
    }
}
