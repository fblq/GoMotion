package com.campus.gomotion.sensorData;

import java.io.Serializable;

/**
 * Author zhong.zhou
 * Date 5/17/16
 * Email qnarcup@gmail.com
 */
public class AngularVelocity implements Serializable {
    private static final long serialVersionUID = 1524604070828785700L;
    private float x;
    private float y;
    private float z;

    public AngularVelocity() {
    }

    public float getX() {
        return x;
    }

    public AngularVelocity setX(float x) {
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public AngularVelocity setY(float y) {
        this.y = y;
        return this;
    }

    public float getZ() {
        return z;
    }

    public AngularVelocity setZ(float z) {
        this.z = z;
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(x).append(" ").append(y).append(" ").append(z).toString();
    }
}
