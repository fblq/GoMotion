package com.campus.gomotion.kind;

import java.io.Serializable;

/**
 * Author: zhong.zhou
 * Date: 16/4/24
 * Email: muxin_zg@163.com
 */
public class Falling implements Serializable {
    private static final long serialVersionUID = 7997745854078576779L;
    private int count;

    public Falling() {
    }

    public Falling(Falling falling) {
        this.count = falling.getCount();
    }


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
