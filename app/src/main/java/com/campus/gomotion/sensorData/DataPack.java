package com.campus.gomotion.sensorData;

import java.io.Serializable;

/**
 * Author zhong.zhou
 * Date 5/17/16
 * Email qnarcup@gmail.com
 */
public class DataPack implements Serializable {
    private static final long serialVersionUID = 4283422089479609982L;
    private Quaternion quaternion;
    private Accelerometer accelerometer;
    private AngularVelocity angularVelocity;

    public DataPack() {
    }

    public DataPack(Quaternion quaternion,Accelerometer accelerometer,AngularVelocity angularVelocity){
        this.quaternion = quaternion;
        this.accelerometer = accelerometer;
        this.angularVelocity = angularVelocity;
    }

    public Quaternion getQuaternion() {
        return quaternion;
    }

    public DataPack setQuaternion(Quaternion quaternion) {
        this.quaternion = quaternion;
        return this;
    }

    public Accelerometer getAccelerometer() {
        return accelerometer;
    }

    public DataPack setAccelerometer(Accelerometer accelerometer) {
        this.accelerometer = accelerometer;
        return this;
    }

    public AngularVelocity getAngularVelocity() {
        return angularVelocity;
    }

    public DataPack setAngularVelocity(AngularVelocity angularVelocity) {
        this.angularVelocity = angularVelocity;
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(quaternion.toString()).append(" ")
                .append(accelerometer.toString()).append(" ")
                .append(angularVelocity.toString()).toString();
    }
}
