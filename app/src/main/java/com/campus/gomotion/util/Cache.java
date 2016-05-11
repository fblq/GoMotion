package com.campus.gomotion.util;

import com.campus.gomotion.sensorData.Quaternion;

import java.util.Objects;

/**
 * Author: zhong.zhou
 * Date: 16/5/9
 * Email: muxin_zg@163.com
 */
public class Cache<T> {
    private final static String TAG = "";
    private int front;
    private int tail;
    private int maxSize;
    private Object[] cache;

    public Cache(int size) {
        this.front = 0;
        this.tail = 0;
        this.maxSize = size;
        cache = new Object[maxSize];
    }

    public void put(T t) {
        if (tail > 49) {
            tail = tail % 50;
            front++;
        }
        cache[tail++] = t;
    }

    public T tail() {
        if (front == tail) {
            throw new IndexOutOfBoundsException("cache empty exception");
        }
        return (T) cache[tail];
    }

    public T front() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("cache empty exception");
        }
        return (T) cache[front];
    }

    public Object[] getCache() {
        return cache;
    }

    public int getSize() {
        if (tail > front) {
            return tail - front;
        } else {
            return maxSize;
        }
    }

    public boolean isEmpty() {
        return (front == tail);
    }

    public void clear() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("cache empty exception");
        }
        front = tail = 0;
    }
}
