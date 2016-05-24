package com.campus.gomotion.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.campus.gomotion.constant.MotionEnum;
import com.campus.gomotion.constant.UIData;
import com.campus.gomotion.constant.UserInfo;
import com.campus.gomotion.kind.Falling;
import com.campus.gomotion.kind.Moving;
import com.campus.gomotion.sensorData.*;
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
    public static Map<Time, Falling> fallingMap = new TreeMap<>();
    public static Map<Time, Moving> walkingMap = new TreeMap<>();
    public static Map<Time, Moving> runningMap = new TreeMap<>();

    public static Map<Time, Float> fallingLog = new TreeMap<>();
    public static Moving totalRunning = new Moving();
    public static Moving totalWalking = new Moving();
    /**
     * 存储小段时间内的数据(当前间隔:1分钟)
     */
    private static Falling falling = new Falling();
    private static Moving running = new Moving();
    private static Moving walking = new Moving();

    /**
     * handle message
     */
    private Handler handler;

    /**
     * 跌倒到爬起来的时间计量
     */
    private static int interval = 0;

    /**
     * 跌倒状态位
     */
    private static boolean isFall = false;

    public MotionStatisticService(Handler handler) {
        this.handler = handler;
    }

    public void motionStatistic(ArrayDeque<DataPack> dataPacks) {
        MotionEnum motionKind = MotionEnum.UNKNOW;
        float averageAcceleration = averageAcceleration(dataPacks);
        float averageAngular = averageAngular(dataPacks);
        /**
         * 对采集的数据进行分析,得出以下规律
         * 当1s内的合力加速度的平均值小于1.1时,处于静止状态,当合力加速的的均值大于1.1且小于1.3时,处于
         * 行走状态,当合力加速度的均值大于1.3时,处于跑步状态.
         * 当1s内的合力角速度的平均值小于50时,处于静止状态,当合力角速度的均值大于50时,处于行走或跑步状态
         */
        /**
         * 排除静止情况
         */
        if (averageAngular > 50 || averageAcceleration > 1.1) {
            /**
             * 跌倒情况
             */
            if (isFalling(dataPacks)) {
                falling.increase();
                isFall = true;
                motionKind = MotionEnum.FALLING;
            }
            if (isFall) {
                long t = System.currentTimeMillis();
                Time time = new Time(t);
                fallingLog.put(time, (float) (interval));
                isFall = false;
                interval = 0;
            }
            interval++;
            long time = 1;
            float distance = calculateDistance(averageAcceleration, time);
            float energyConsumption = calculateEnergyConsumption(UserInfo.WEIGHT, averageAcceleration, time);
            /**
             * 根据角速度大于120的频率判断行走/跑步
             */
            boolean isRun = isLargeAngularHighFrequency(dataPacks);
            /**
             * 正常行走
             */
            if (!isRun || averageAcceleration < 1.3) {
                Moving moving = new Moving(time, distance, 1, energyConsumption);
                walking.add(moving);
                totalWalking.add(moving);
                motionKind = MotionEnum.WALKING;
            }
            /**
             * 正常跑步情况
             */
            if (isRun || (averageAcceleration > 1.3 && averageAcceleration < 2)) {
                Moving moving = new Moving(time, distance, 2, energyConsumption);
                running.add(moving);
                totalRunning.add(moving);
                motionKind = MotionEnum.RUNNING;
            }
        } else {
            Log.v(TAG, "静止状态");
            motionKind = MotionEnum.STILLING;
        }
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        if (totalWalking != null) {
            /**
             * 保留浮点数后两位
             */
            float temp = (float) (totalWalking.getTime() / 60.0);
            float walkTime = (float) (Math.round(temp * 100)) / 100;
            float walkDistance = (float) (Math.round(totalWalking.getDistance() * 100)) / 100;
            bundle.putString(UIData.WALK_TIME, String.valueOf(walkTime));
            bundle.putString(UIData.WALK_DISTANCE, String.valueOf(walkDistance));
        } else {
            bundle.putString(UIData.WALK_TIME, "0");
            bundle.putString(UIData.WALK_DISTANCE, "0");
        }
        if (totalRunning != null) {
            float temp = (float) (totalRunning.getTime() / 60.0);
            float runTime = (float) (Math.round(temp * 100)) / 100;
            float runDistance = (float) (Math.round(totalRunning.getDistance() * 100)) / 100;
            bundle.putString(UIData.RUN_TIME, String.valueOf(runTime));
            bundle.putString(UIData.RUN_DISTANCE, String.valueOf(runDistance));
        } else {
            bundle.putString(UIData.RUN_TIME, "0");
            bundle.putString(UIData.RUN_DISTANCE, "0");
        }
        bundle.putString(UIData.FALLING_COUNT, String.valueOf(calculateFallingTotalCount()));
        float averageFallingTime = (float) (Math.round(calculateAverageFallingTime() * 100)) / 100;
        bundle.putString(UIData.FALLING_AVERAGE_TIME, String.valueOf(averageFallingTime));
        bundle.putString(UIData.MOTION_KING, motionKind.getName());
        message.setData(bundle);
        handler.sendMessage(message);
    }

    /**
     * 添加数据到每日的运动记录中
     */
    public void loadDataToCache() {
        long t = System.currentTimeMillis() - 60 * 1000;
        Time time = new Time(t);
        if (falling != null) {
            Falling temp = new Falling(falling);
            if(fallingMap.containsKey(time)){
                Falling falling = fallingMap.get(time);
                falling.add(temp);
            }else{
                fallingMap.put(time, temp);
            }
            falling.clear();
        }
        if (running != null) {
            Moving temp = new Moving(running);
            if(runningMap.containsKey(time)){
                Moving moving = runningMap.get(time);
                moving.add(temp);
            }else{
                runningMap.put(time, temp);
            }
            running.clear();
        }
        if (walking != null) {
            Moving temp = new Moving(walking);
            if(walkingMap.containsKey(time)){
                Moving moving = walkingMap.get(time);
                moving.add(temp);
            }else{
                walkingMap.put(time, temp);
            }
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
     * 计算1s内的合力加速度均值(单位:N/s)
     *
     * @param dataPacks ArrayDeque<DataPack>
     * @return float
     */
    public static float averageAcceleration(ArrayDeque<DataPack> dataPacks) {
        float sum = 0, i = 0, temp;
        Accelerometer acceleration;
        if (dataPacks != null && dataPacks.size() > 0) {
            for (DataPack dataPack : dataPacks) {
                acceleration = dataPack.getAccelerometer();
                temp = PhysicalConversionUtil.calculateGeometricMeanAcceleration(acceleration);
                sum += temp;
                i++;
            }
            return (sum / i);
        } else {
            return 0;
        }
    }

    /**
     * 计算1s内的合力角速度均值(单位:N/s)
     *
     * @param dataPacks ArrayDeque<DataPack>
     * @return float
     */
    public static float averageAngular(ArrayDeque<DataPack> dataPacks) {
        float sum = 0, i = 0, temp;
        AngularVelocity angularVelocity;
        if (dataPacks != null && dataPacks.size() > 0) {
            for (DataPack dataPack : dataPacks) {
                angularVelocity = dataPack.getAngularVelocity();
                temp = PhysicalConversionUtil.calculateGeometricMeanAngular(angularVelocity);
                sum += temp;
                i++;
            }
            return (sum / i);
        } else {
            return 0;
        }
    }

    /**
     * 计算1s内合力角速度大于100的次数
     *
     * @param dataPacks ArrayDeque<DataPack>
     * @return boolean
     */
    private boolean isLargeAngularHighFrequency(ArrayDeque<DataPack> dataPacks) {
        int count = 0;
        float temp;
        if (dataPacks != null && dataPacks.size() > 0) {
            for (DataPack dataPack : dataPacks) {
                temp = PhysicalConversionUtil.calculateGeometricMeanAngular(dataPack.getAngularVelocity());
                if (temp > 120) {
                    count++;
                }
            }
            return count > dataPacks.size() - 10;
        } else {
            return false;
        }
    }

    private boolean isFalling(ArrayDeque<DataPack> dataPacks) {
        float temp1, temp2;
        int count1 = 0, count2 = 0;
        if (dataPacks != null && dataPacks.size() > 0) {
            for (DataPack dataPack : dataPacks) {
                temp1 = PhysicalConversionUtil.calculateGeometricMeanAcceleration(dataPack.getAccelerometer());
                temp2 = PhysicalConversionUtil.calculateGeometricMeanAngular(dataPack.getAngularVelocity());
                if (temp1 > 4) {
                    count1++;
                }
                if (temp2 > 400) {
                    count2++;
                }
            }
            return ((count1 > 0 && count1 <= 5) || (count2 > 0 && count2 <= 5));
        } else {
            return false;
        }
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
        if (fallingLog != null) {
            return fallingLog.size();
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
        Map<Time, Double> resultMap = new TreeMap<>();
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
        Map<Time, Double> resultMap = new TreeMap<>();
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
