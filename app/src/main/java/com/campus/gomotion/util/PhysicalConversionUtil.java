package com.campus.gomotion.util;

import com.campus.gomotion.sensorData.Accelerometer;
import com.campus.gomotion.sensorData.AttitudeAngle;
import com.campus.gomotion.sensorData.Quaternion;

/**
 * Author: zhong.zhou
 * Date: 16/4/24
 * Email: muxin_zg@163.com
 */
public class PhysicalConversionUtil {
    /**
     * transfer quaternion to attitude angle
     *
     * @param quaternion Quaternion
     * @return AttitudeAngle
     */
    public static AttitudeAngle quaternionToAttitudeAngle(Quaternion quaternion) {
        AttitudeAngle attitudeAngle = new AttitudeAngle();
        float w = quaternion.getW();
        float x = quaternion.getX();
        float y = quaternion.getY();
        float z = quaternion.getZ();
        attitudeAngle.setYaw((float) Math.atan2(2 * w * z + 2 * x * y, 1 - 2 * y * y - 2 * z * z) * (float) (180 / 3.1415));
        attitudeAngle.setPitch((float) Math.asin(2 * w * y - 2 * z * x) * (float) (180 / 3.1415));
        attitudeAngle.setRoll((float) Math.atan2(2 * w * x + 2 * y * z, 1 - 2 * x * x - 2 * y * y) * (float) (180 / 3.1415));
        return attitudeAngle;
    }

    /**
     * calculate geometric mean acceleration on the basis of attitude angle
     *
     * @param attitudeAngle AttitudeAngle
     * @return float
     */
    /*public static float calculateGeometricMeanAcceleration(AttitudeAngle attitudeAngle) {
        float roll = attitudeAngle.getRoll();
        float pitch = attitudeAngle.getPitch();
        float yaw = attitudeAngle.getYaw();
        float ax = -(float) Math.sin(pitch);
        float ay = (float) (Math.sin(roll) * Math.cos(pitch));
        float az = (float) (Math.cos(roll) * Math.cos(pitch));
        return (float) Math.sqrt((double) (ax * ax + ay * ay + az * az) / (double) (3));
    }*/

    /**
     * calculate geometric mean acceleration on the basis of accelerometer
     *
     * @param accelerometer Accelerometer
     * @return float
     */
    public static float calculateGeometricMeanAcceleration(Accelerometer accelerometer) {
        float x = accelerometer.getX();
        float y = accelerometer.getY();
        float z = accelerometer.getZ();
        return (float) Math.sqrt(x * x + y * y + z * z);
    }
}
