package com.indoorlocalizer.app.activity.common.model;

/**
 * Created by federicostivani on 14/06/14.
 */
public class InfrastructureMap {
    String mapName;
    int rpNumber;
    String mapImagePath;

    public InfrastructureMap() {
        this.mapName = "";
        this.rpNumber = 1;
        this.mapImagePath = "";
    }

    public InfrastructureMap(String mapName, int rpNumber, String mapImagePath) {
        this.mapName = mapName;
        this.rpNumber = rpNumber;
        this.mapImagePath = mapImagePath;
    }

    public String getMapName() {
        return mapName;
    }

    public int getRpNumber() {
        return rpNumber;
    }

    public String getMapImagePath() {
        return mapImagePath;
    }
}
