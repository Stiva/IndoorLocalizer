package com.indoorlocalizer.app.activity.common.model;

import java.util.ArrayList;

/**
 * Created by federicostivani on 04/06/14.
 */
public class ReferencePoint {
    private int id;
    private ArrayList<AccessPoint> fingerprint;
    private String location;
    public ReferencePoint(String location,int id, ArrayList<AccessPoint> accessPoints){
        this.location=location;
        this.id=id;
        this.fingerprint=accessPoints;
    }
}
