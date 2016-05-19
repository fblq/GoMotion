package com.campus.gomotion.service;

import com.campus.gomotion.constant.UserInfo;
import com.campus.gomotion.kind.Falling;
import com.campus.gomotion.kind.Moving;
import com.campus.gomotion.sensorData.Accelerometer;
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
    public static Moving totalRunning = new Moving();
    public static Moving totalWalking = new Moving();
    /**
     * 存储小段时间内的数据(当前间隔:1分钟)
     */
    private static Falling falling = new Falling();
    private static Moving running = new Moving();
    private static Moving walking = new Moving();

    /**
     * 跌倒到爬起来的时间计量
     */
    private static int interval = 0;

    /**
     * 跌倒状态位
     */
    private static boolean isFall = false;

    /**
     * 更新加速度几何均值的峰值点(10s一次)
     *
     * @param dataPack DataPack
     * @return boolean
     */
    private boolean updateExtremum(DataPack dataPack) {
        float interPoint = 0, endPoint = 0, accelerationMin = 0, accelerationMax = 0;
        int stepCount = 0, t = 0;
        boolean flag = false;
        AttitudeAngle attitudeAngle = PhysicalConversionUtil.quaternionToAttitudeAngle(dataPack.getQuaternion());
        float temp = PhysicalConversionUtil.calculateGeometricMeanAcceleration(dataPack.getAccelerometer());
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
        float tailYaw = tailAttitudeAngle.getYaw();
        float tailPitch = tailAttitudeAngle.getPitch();
        float tailRoll = tailAttitudeAngle.getRoll();
        float frontYaw = frontAttitudeAngle.getYaw();
        float frontPitch = frontAttitudeAngle.getPitch();
        float frontRoll = frontAttitudeAngle.getRoll();

        float averageAcceleration = averageAcceleration(dataPacks);
        /**
         * 跌倒情况
         */
        if (frontYaw > 30 || frontYaw < -30 || frontPitch > 45 || frontPitch < -30) {
            if (tailYaw > 30 || tailYaw < -30 || tailPitch > 45 || tailPitch < -30) {
                falling.increase();
                isFall = true;
            }
        }
        if (isFall) {
            long t = System.currentTimeMillis();
            Time time = new Time(t);
            fallingLog.put(time, (float) (interval));
            isFall = false;
        }
        interval++;
        long time = 1;
        float distance = calculateDistance(averageAcceleration, time);
        float energyConsumption = calculateEnergyConsumption(UserInfo.WEIGHT, averageAcceleration, time);
        /**
         * 正常行走/静止情况
         */
        if (frontYaw > -10 && frontYaw < 10 && frontPitch > -10 && frontPitch < 10) {
            if (tailYaw > -10 && tailYaw < 10 && tailYaw > -10 && tailYaw < 10) {
                Moving moving = new Moving(time, distance, 1, energyConsumption);
                walking.add(moving);
                totalWalking.add(moving);
                /*if (averageAcceleration > 1 && averageAcceleration < 2) {
                    Moving moving = new Moving(time, distance, 1, energyConsumption);
                    walking.add(moving);
                    totalWalking.add(moving);
                }*/
            }
        }
        /**
         * 正常跑步情况
         */
        if ((frontYaw > 10 && frontYaw < 30) || (frontYaw > -30 && frontYaw < -10) || (frontPitch > 10 && frontPitch < 45) || (frontPitch > -30 && frontPitch < -10)) {
            if ((tailYaw > 10 && tailYaw < 30) || (tailYaw > -30 && tailYaw < -10) || (tailPitch > 10 && tailPitch < 45) || (tailPitch > -30 && tailPitch < -10)) {
                Moving moving = new Moving(time, distance, 1, energyConsumption);
                running.add(moving);
                totalRunning.add(moving);
               /* if (averageAcceleration > 2) {
                    Moving moving = new Moving(time, distance, 1, energyConsumption);
                    running.add(moving);
                    totalRunning.add(moving);
                }*/
            }
        }
    }

    /**
     * 添加数据到每日的运动记录中
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
     * 清空缓存
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
     * 计算1s内的平均加速度(单位:N/s)
     *
     * @param dataPacks DataPack[]
     * @return float
     */
    private float averageAcceleration(ArrayDeque<DataPack> dataPacks) {
        float sum = 0, i, temp;
        Accelerometer acceleration;
        i = dataPacks.size();
        for (DataPack dataPack : dataPacks) {
            acceleration = dataPack.getAccelerometer();
            temp = PhysicalConversionUtil.calculateGeometricMeanAcceleration(acceleration);
            sum += temp;
        }
        return (sum / i);
    }

    /**
     * 根据数据发送频率20ms/次计算时间(单位:s)
     *
     * @param t int
     * @return long
     */
    private float calculateTime(int t) {
        return (float) (t * 20) / (float) 1000;
    }

    /**
     * 根据平均加速度求位移(单位:m)
     *
     * @param a float
     * @return float
     */
    private float calculateDistance(float a, float t) {
        return (float) (1.0 / 2.0) * a * t * t;
    }

    /**
     * 根据波峰/波谷的数目计算步数(单位:步)
     *
     * @param count int
     * @return long
     */
    private long calculateStep(int count) {
        return (long) (count / 2);
    }

    /**
     * 根据外力作功的物理学公式计算卡路里消耗(单位:cal)
     *
     * @param weight float
     * @return float
     */
    private float calculateEnergyConsumption(float weight, float acceleration, float t) {
        return (float) ((float) (1.0 / 4.0) * (float) (0.014 * weight * 9.8 * acceleration * t * t) / 4.18);
    }

    /**
     * 计算跌倒总次数(单位:次)
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
     * 计算平均跌倒时间(单位:s)
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
     * 计算运动完成量(单位:步)
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
     * 合并跑步和行走的步数(单位:步)
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
     * 合并跑步和行走的卡路里消耗(单位:cal)
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
