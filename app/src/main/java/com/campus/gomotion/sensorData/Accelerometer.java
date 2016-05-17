package com.campus.gomotion.sensorData;

import java.io.Serializable;

/**
 * Author zhong.zhou
 * Date 5/17/16
 * Email qnarcup@gmail.com
 */
public class Accelerometer implements Serializable {
    private static final long serialVersionUID = 4961585361065898326L;
    private float x;
    private float y;
    private float z;

    public Accelerometer() {
    }

    public float getZ() {
        return z;
    }

    public Accelerometer setZ(float z) {
        this.z = z;
        return this;
    }

    public float getX() {
        return x;
    }

    public Accelerometer setX(float x) {
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public Accelerometer setY(float y) {
        this.y = y;
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(x).append(" ").append(y).append(" ").append(z).toString();
    }
}
