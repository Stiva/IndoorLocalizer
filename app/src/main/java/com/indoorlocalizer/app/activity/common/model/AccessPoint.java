package com.indoorlocalizer.app.activity.common.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by federicostivani on 01/06/14.
 */
public class AccessPoint implements Parcelable{
    private String SSID;
    private String map;
    private String BSSID;
    private String capabilities;
    private int rp;
    private int level;
    private int frequency;
    private int hits;

    public AccessPoint() {
        this.map="";
        this.rp=-1;
        this.SSID="";
        this.BSSID="";
        this.capabilities="";
        this.level=-1;
        this.frequency=-1;
        this.hits=-1;
    }

    public AccessPoint(String map, int rp, String ssid, String bssid, String capabilities, int level, int frequency) {
        this.map=map;

        this.rp=rp;
        this.SSID = ssid;
        this.BSSID = bssid;
        this.capabilities = capabilities;
        this.level = level;
        this.frequency = frequency;
        this.hits=0;
    }

    public String getMap() { return this.map;  }

    public int getRp() { return this.rp;}

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {this.SSID,
                String.valueOf(this.rp),
                this.BSSID,
                this.capabilities,
                String.valueOf(this.level),
                String.valueOf(this.frequency),
                String.valueOf(this.hits)});
    }

    public void hit() {
        hits++;
    }

    public int getHits() {
        return hits;
    }
}
