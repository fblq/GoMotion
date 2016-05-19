package com.campus.gomotion.kind;

import java.io.Serializable;

/**
 * Author: zhong.zhou
 * Date: 16/4/24
 * Email: muxin_zg@163.com
 */
public class Moving implements Serializable {
    private static final long serialVersionUID = 8090854784395801378L;
    private long time;
    private float distance;
    private long step;
    private float energyConsumption;

    public Moving() {
    }

    public Moving(Moving moving) {
        this.time = moving.getTime();
        this.distance = moving.getDistance();
        this.step = moving.getStep();
        this.energyConsumption = moving.getEnergyConsumption();
    }

    public Moving(long time, float distance, long step, float energyConsumption) {
        this.time = time;
        this.distance = distance;
        this.step = step;
        this.energyConsumption = energyConsumption;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getEnergyConsumption() {
        return energyConsumption;
    }

    public void setEnergyConsumption(float energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void clear() {
        this.time = 0;
        this.distance = 0;
        this.step = 0;
        this.energyConsumption = 0;
    }

    public void add(Moving moving) {
        if (moving != null) {
            this.time += moving.getTime();
            this.distance += moving.getDistance();
            this.step += moving.getStep();
            this.energyConsumption += moving.getEnergyConsumption();
        }
    }
}
