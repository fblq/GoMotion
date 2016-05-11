package com.campus.gomotion.sensorData;

/**
 * Author: zhong.zhou
 * Date: 16/4/24
 * Email: muxin_zg@163.com
 */
public class AttitudeAngle {
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

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
