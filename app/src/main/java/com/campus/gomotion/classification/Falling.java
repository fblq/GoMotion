package com.campus.gomotion.classification;

/**
 * Author: zhong.zhou
 * Date: 16/4/24
 * Email: muxin_zg@163.com
 */
public class Falling {
    private int count;

    public Falling(){}

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void increase() {
        this.count++;
    }

    public void clear() {
        this.count = 0;
    }

    public void add(Falling falling) {
        this.count += falling.getCount();
    }
}
