package com.campus.gomotion.sensorData;

import java.io.Serializable;

/**
 * Author: zhong.zhou
 * Date: 16/4/24
 * Email: muxin_zg@163.com
 */
public class AttitudeAngle implements Serializable {
    private static final long serialVersionUID = 7351840228628361843L;
    /**
     * angle of rotation about the x axis
     * 偏航角
     */
    private float yaw;
    /**
     * Angle of rotation about the y axis
     * 俯仰角
     */
    private float pitch;
    /**
     * Angle of rotation about the z axis
     * 横滚角
     */
    private float roll;

    public AttitudeAngle() {
    }

    public float getYaw() {
        return yaw;
    }

    public AttitudeAngle setYaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    public float getPitch() {
        return pitch;
    }

    public AttitudeAngle setPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public float getRoll() {
        return roll;
    }

    public AttitudeAngle setRoll(float roll) {
        this.roll = roll;
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(yaw).append("|")
                .append(pitch).append("|").append(roll).toString();
    }
}
