package com.indoorlocalizer.app.activity.common.model;

import java.io.Serializable;

/**
 * Object that describes an Access Point with all its features.
 */
public class AccessPoint implements Serializable {
    private String SSID;
    private String map;
    private String BSSID;
    private String capabilities;
    private int rp;
    private int level;
    private int frequency;
    private int hits;

    public AccessPoint(String map, int rp, String ssid, String bssid, String capabilities, int level, int frequency) {
        this.map = map;
        this.rp = rp;
        this.SSID = ssid;
        this.BSSID = bssid;
        this.capabilities = capabilities;
        this.level = level;
        this.frequency = frequency;
        this.hits = 0;
    }

    public AccessPoint(String ssid, int level, int frequency) {
        this.map = "";
        this.SSID = ssid;
        this.BSSID = "";
        this.capabilities = "";
        this.level = level;
        this.frequency = frequency;
        this.hits = 0;
    }

    public String getMap() {
        return this.map;
    }
    public void setMap(String mapName) {
        this.map=mapName;
    }

    public int getRp() {
        return this.rp;
    }

    public void setRp(int rp) {
        this.rp = rp;
    }

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

    public void hit() {
        hits++;
    }

    public int getHits() {
        return hits;
    }
}
