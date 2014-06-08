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

    public String getBSSID() {
        return BSSID;
    }

    public String getCapabilities() {
        return capabilities;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {
                getMap(),
                String.valueOf(getRp()),
                getSSID(),
                getBSSID(),
                getCapabilities(),
                String.valueOf(getLevel()),
                String.valueOf(getFrequency()),
                String.valueOf(getHits())});
    }

    public void hit() {
        hits++;
    }

    public int getHits() {
        return hits;
    }
}
