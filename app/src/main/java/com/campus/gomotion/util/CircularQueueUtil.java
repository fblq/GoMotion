package com.campus.gomotion.util;

import com.campus.gomotion.sensorData.Quaternion;

import java.util.Objects;

/**
 * Author: zhong.zhou
 * Date: 16/5/9
 * Email: muxin_zg@163.com
 */
public class CircularQueueUtil<T> {
    private final static String TAG = "CircularQueueUtil";
    private int front;
    private int tail;
    private int maxSize;
    private T[] cache;

    public CircularQueueUtil(int size) {
        this.front = 0;
        this.tail = 0;
        this.maxSize = size;
        cache = (T[])new Object[maxSize];
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
        return cache[tail-1];
    }

    public T front() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("cache empty exception");
        }
        return cache[front];
    }

    public T[] getCache() {
        return cache;
    }

    public int getSize() {
        if (tail > front) {
            return tail - front;
        } else {
            return maxSize;
        }
    }

    public boolean isFull() {
        return (getSize() == 50);
    }

    public boolean isEmpty() {
        return (front == tail);
    }

    public void clear() {
        front = tail = 0;
    }
}
