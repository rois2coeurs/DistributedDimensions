package com.edouardcourty.dd.common.model;

public class LocationData {
    public double x, y, z;
    public float yaw, pitch;

    public static LocationData of(double x, double y, double z, float yaw, float pitch) {
        LocationData ld = new LocationData();
        ld.x = x; ld.y = y; ld.z = z;
        ld.yaw = yaw; ld.pitch = pitch;
        return ld;
    }
}
