package com.campus.gomotion.classification;

/**
 * Author: zhong.zhou
 * Date: 16/4/24
 * Email: muxin_zg@163.com
 */
public class Moving {
    private float time;
    private float distance;
    private long step;
    private float energyConsumption;

    public Moving(){}

    public Moving(float time, float distance, long step, float energyConsumption){
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

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public void clear(){
        this.time = 0;
        this.distance = 0;
        this.step = 0;
        this.energyConsumption = 0;
    }

    public void add(Moving moving){
        this.time += moving.getTime();
        this.distance += moving.getDistance();
        this.step += moving.getStep();
        this.energyConsumption += moving.getEnergyConsumption();
    }
}
