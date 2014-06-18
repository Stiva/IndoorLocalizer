package com.indoorlocalizer.app.activity.common.model;

/**
 * Created by federicostivani on 18/06/14.
 */
public class ReferencePoint {
    private String mapName;
    private String name;
    private int id;

    public ReferencePoint(String mapName, String name, int id) {
        this.mapName = mapName;
        this.name = name;
        this.id = id;
    }

    public String getMapName() {
        return mapName;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
