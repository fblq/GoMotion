package com.campus.gomotion.service;

import com.campus.gomotion.kind.Falling;
import com.campus.gomotion.kind.Moving;
import com.campus.gomotion.sensorData.AttitudeAngle;
import com.campus.gomotion.sensorData.DataPack;
import com.campus.gomotion.sensorData.Quaternion;
import com.campus.gomotion.util.PhysicalConversionUtil;

import java.sql.Time;
import java.util.*;

/**
 * Author: zhong.zhou
 * Time: 16/4/24
 * Email: muxin_zg@163.com
 */
public class MotionStatisticService {
    private static final String TAG = "MotionStatisticService";
    public static Map<Time, Falling> fallingMap = new HashMap<>();
    public static Map<Time, Moving> walkingMap = new HashMap<>();
    public static Map<Time, Moving> runningMap = new HashMap<>();

    public static Map<Time, Float> fallingLog = new HashMap<>();
    public static Moving totalRunning;
    public static Moving totalWalking;
    /**
     * 存储小段时间内的数据(当前间隔:1分钟)
     */
    private Falling falling;
    private Moving running;
    private Moving walking;

    /**
     * 寻找极值点的辅助变量
     */
    private float endPoint;
    private float interPoint;
    private float accelerationMin;
    private float accelerationMax;

    /**
     * 计步变量
     */
    private int stepCount;

    /**
     * 行走或跑步的时间计量
     */
    private int t;

    /**
     * 跌倒到爬起来的时间计量
     */
    private int interval;

    public MotionStatisticService() {
        falling = new Falling();
        running = new Moving();
        walking = new Moving();
        endPoint = 0;
        interPoint = 0;
        accelerationMax = 0;
        accelerationMin = 0;
        t = 0;
        interval = 0;
    }

    /**
     * upTime the extremum of acceleration geometric mean
     *
     * @param dataPack DataPack
     * @return boolean
     */
    private boolean updateExtremum(DataPack dataPack) {
        boolean flag = false;
        AttitudeAngle attitudeAngle = PhysicalConversionUtil.quaternionToAttitudeAngle(dataPack.getQuaternion());
        float temp = PhysicalConversionUtil.calculateGeometricMeanAcceleration(attitudeAngle);
        if (interPoint < endPoint && interPoint < temp) {
            accelerationMin = interPoint;
            flag = true;
        } else if (interPoint > endPoint && interPoint > temp) {
            accelerationMax = interPoint;
            flag = true;
        }
        endPoint = interPoint;
        interPoint = temp;
        stepCount++;
        t++;
        interval++;
        return flag;
    }

    public void motionStatistic(ArrayDeque<DataPack> dataPacks) {
        Quaternion tailQuaternion = dataPacks.getLast().getQuaternion();
        Quaternion frontQuaternion = dataPacks.getFirst().getQuaternion();
        AttitudeAngle tailAttitudeAngle = PhysicalConversionUtil.quaternionToAttitudeAngle(tailQuaternion);
        AttitudeAngle frontAttitudeAngle = PhysicalConversionUtil.quaternionToAttitudeAngle(frontQuaternion);
        float tailAcceleration = PhysicalConversionUtil.calculateGeometricMeanAcceleration(tailAttitudeAngle);
        float frontAcceleration = PhysicalConversionUtil.calculateGeometricMeanAcceleration(frontAttitudeAngle);
        float tailYaw = tailAttitudeAngle.getYaw();
        float tailPitch = tailAttitudeAngle.getPitch();
        float tailRoll = tailAttitudeAngle.getRoll();
        float frontYaw = frontAttitudeAngle.getYaw();
        float frontPitch = frontAttitudeAngle.getPitch();
        float frontRoll = frontAttitudeAngle.getRoll();
        /**
         * 跌倒情况
         */
        if (frontYaw > 30 || frontYaw < -30 || frontPitch > 45 || frontPitch < -30) {
            if (tailYaw > 30 || tailYaw < -30 || tailPitch > 45 || tailPitch < -30) {
                falling.increase();
                /**
                 * 获得系统时间并计时,更新fallingLog
                 */
                long t = System.currentTimeMillis();
                Time time = new Time(t);
                if (interval != 0) {
                    fallingLog.put(time, (Float) ((float) (interval * 20) / (float) 1000));
                    interval = 0;
                }
            }
        }
        /**
         * 正常行走/静止情况
         */
        if (frontYaw > -10 && frontYaw < 10 && frontPitch > -10 && frontPitch < 10) {
            if (tailYaw > -10 && tailYaw < 10 && tailYaw > -10 && tailYaw < 10) {

            }
        }
        /**
         * 正常跑步情况
         */
        if ((frontYaw > 10 && frontYaw < 30) || (frontYaw > -30 && frontYaw < -10) || (frontPitch > 10 && frontPitch < 45) || (frontPitch > -30 && frontPitch < -10)) {
            if ((tailYaw > 10 && tailYaw < 30) || (tailYaw > -30 && tailYaw < -10) || (tailPitch > 10 && tailPitch < 45) || (tailPitch > -30 && tailPitch < -10)) {

            }
        }
        float averageAcceleration = averageAcceleration(dataPacks);
        /**
         * 不是跌倒的状态下,找到极值后进行计算并判断情况
         */
        for (DataPack dataPack : dataPacks) {
            if (updateExtremum(dataPack)) {
                /**
                 * 依据一分钟内的加速度几何均值的平均值判断行走和跑步两种状态
                 */
                float temp = (float) (1 / 2) * (accelerationMin + accelerationMax);
                float time = calculateTime(t);
                float distance = calculateDistance(temp, time);
                long step = calculateStep(stepCount);
                /**
                 * 76为用户体重(单位:kg)
                 */
                float energyConsumption = calculateEnergyConsumption(76, time);
                Moving moving = new Moving(time, distance, step, energyConsumption);
                /**
                 * 根据加速度的几何均值作为判别行走和跑步两种状态
                 */
                if (averageAcceleration > 2) {
                    running.add(moving);
                    totalRunning.add(moving);
                } else {
                    /**
                     * 行走的情况
                     */
                    walking.add(moving);
                    totalWalking.add(moving);
                }
            }
        }
    }

    /**
     * every 1 minutes, load data to cache to statistic daily motion
     */
    public void loadDataToCache() {
        long t = System.currentTimeMillis() - 60 * 1000;
        Time time = new Time(t);
        if (falling != null) {
            Falling temp = new Falling(falling);
            fallingMap.put(time, temp);
            falling.clear();
        }
        if (running != null) {
            Moving temp = new Moving(running);
            runningMap.put(time, temp);
            running.clear();
        }
        if (walking != null) {
            Moving temp = new Moving(walking);
            walkingMap.put(time, temp);
            walking.clear();
        }
    }

    /**
     * clear cache at 0:00 every day
     */
    public void clearCache() {
        if (totalWalking != null) {
            totalWalking.clear();
        }
        if (totalRunning != null) {
            totalRunning.clear();
        }
        if (fallingMap != null) {
            fallingMap.clear();
        }
        if (walkingMap != null) {
            walkingMap.clear();
        }
        if (runningMap != null) {
            runningMap.clear();
        }
    }

    /**
     * caculate the average of acceleration in one minute
     *
     * @param dataPacks DataPack[]
     * @return float
     */
    private float averageAcceleration(ArrayDeque<DataPack> dataPacks) {
        float sum = 0, i = 0;
        AttitudeAngle attitudeAngle;
        Float acceleration;
        i = dataPacks.size();
        for (DataPack dataPack : dataPacks) {
            attitudeAngle = PhysicalConversionUtil.quaternionToAttitudeAngle(dataPack.getQuaternion());
            acceleration = PhysicalConversionUtil.calculateGeometricMeanAcceleration(attitudeAngle);
            sum += acceleration;
        }
        return (sum / i);
    }

    /**
     * calculate time
     *
     * @param t int
     * @return long
     */
    private float calculateTime(int t) {
        return (float) (t * 20) / (float) 1000;
    }

    /**
     * calculate distance
     *
     * @param a float
     * @return float
     */
    private float calculateDistance(float a, float t) {
        return (float) (1 / 2) * a * (float) t * (float) t;
    }

    /**
     * calculate steps
     *
     * @param count int
     * @return long
     */
    private long calculateStep(int count) {
        return (long) (count / 2);
    }

    /**
     * calculate energy consumption
     *
     * @param weight float
     * @return float
     */
    private float calculateEnergyConsumption(float weight, float t) {
        return (float) ((float) (1 / 4) * (float) (0.014 * weight * 9.8 * (accelerationMax - accelerationMin) * (float) (t * t)) / 4.18);
    }

    /**
     * calculate falling total count
     *
     * @return int
     */
    public static int calculateFallingTotalCount() {
        int result = 0;
        if (fallingMap != null) {
            Collection<Falling> values = fallingMap.values();
            for (Falling falling : values) {
                result += falling.getCount();
            }
        }
        return result;
    }

    /**
     * calculate average falling time
     *
     * @return float
     */
    public static float calculateAverageFallingTime() {
        float totalTime = 0;
        int size = 0;
        if (fallingLog != null) {
            Collection<Float> values = fallingLog.values();
            size = fallingLog.size();
            for (Float value : values) {
                totalTime += value;
            }
        }
        return (float) totalTime / size;
    }

    /**
     * calculate movement completion
     *
     * @return long
     */
    public static long calculateCompletion() {
        long result = 0l;
        if (totalWalking != null && totalRunning != null) {
            result = totalWalking.getStep() + totalRunning.getStep();
        }
        return result;
    }

    /**
     * calculate total steps
     *
     * @return Map<Time,Double>
     */
    public static Map<Time, Double> calculateTotalStep() {
        Map<Time, Double> resultMap = new HashMap<>();
        Iterator<Time> walkIterator = walkingMap.keySet().iterator();
        Iterator<Time> runIterator = runningMap.keySet().iterator();
        while (walkIterator.hasNext()) {
            Time key = walkIterator.next();
            double walkStep = (double) walkingMap.get(key).getStep();
            double runStep = (double) runningMap.get(key).getStep();
            resultMap.put(key, walkStep + runStep);
        }
        return resultMap;
    }

    /**
     * calculate total caloriesData
     *
     * @return Map<Time,Double>
     */
    public static Map<Time, Double> calculateTotalCalories() {
        Map<Time, Double> resultMap = new HashMap<>();
        Iterator<Time> walkIterator = walkingMap.keySet().iterator();
        Iterator<Time> runIterator = runningMap.keySet().iterator();
        while (walkIterator.hasNext()) {
            Time key = walkIterator.next();
            double walkStep = (double) walkingMap.get(key).getEnergyConsumption();
            double runStep = (double) runningMap.get(key).getEnergyConsumption();
            resultMap.put(key, walkStep + runStep);
        }
        return resultMap;
    }
}
