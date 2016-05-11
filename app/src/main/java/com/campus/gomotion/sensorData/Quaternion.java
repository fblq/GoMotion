package com.campus.gomotion.sensorData;

/**
 * Author: zhong.zhou
 * Date: 16/4/24
 * Email: muxin_zg@163.com
 */
public class Quaternion {
    private float w;
    private float x;
    private float y;
    private float z;

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(w).append(" & ").append(x).append(" & ")
                .append(y).append(" & ").append(z).toString();
    }
}
