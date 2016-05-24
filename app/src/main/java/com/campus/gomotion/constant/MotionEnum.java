package com.campus.gomotion.constant;

/**
 * Author zhong.zhou
 * Date 16/5/24
 * Email qnarcup@gmail.com
 */
public enum MotionEnum {
    STILLING("静止", 0), WALKING("行走", 1), RUNNING("跑步", 2), FALLING("跌倒", 3), UNKNOW("未分类", 4);
    private String name;
    private int value;

    MotionEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public static MotionEnum valueOfEnum(int value) {
        for (MotionEnum motionEnum : values()) {
            if (motionEnum.getValue() == value) {
                return motionEnum;
            }
        }
        return UNKNOW;
    }

    public static MotionEnum nameOfEnum(String name) {
        for (MotionEnum motionEnum : values()) {
            if (motionEnum.getName().equals(name)) {
                return motionEnum;
            }
        }
        return UNKNOW;
    }

    public static String getMotionName(int value) {
        for (MotionEnum motionEnum : values()) {
            if (motionEnum.getValue() == value) {
                return motionEnum.getName();
            }
        }
        return UNKNOW.getName();
    }

    public String getName() {
        return name;
    }

    public MotionEnum setName(String name) {
        this.name = name;
        return this;
    }

    public int getValue() {
        return value;
    }

    public MotionEnum setValue(int value) {
        this.value = value;
        return this;
    }
}
