package com.campus.gomotion.sensorData;

import java.io.Serializable;

/**
 * Author: zhong.zhou
 * Date: 16/4/24
 * Email: muxin_zg@163.com
 */
public class Quaternion implements Serializable {
    private static final long serialVersionUID = 9077027558781178425L;
    private float w;
    private float x;
    private float y;
    private float z;

    public Quaternion() {
    }

    public float getW() {
        return w;
    }

    public Quaternion setW(float w) {
        this.w = w;
        return this;
    }

    public float getX() {
        return x;
    }

    public Quaternion setX(float x) {
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public Quaternion setY(float y) {
        this.y = y;
        return this;
    }

    public float getZ() {
        return z;
    }

    public Quaternion setZ(float z) {
        this.z = z;
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(w).append(" ").append(x).append(" ")
                .append(y).append(" ").append(z).toString();
    }
}
