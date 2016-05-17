package com.campus.gomotion.util;

import java.util.ArrayDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author zhong.zhou
 * Date 5/16/16
 * Email qnarcup@gmail.com
 */
public class CacheUtil<T> {
    /**
     * 锁对象
     */
    private final Lock lock = new ReentrantLock();
    /**
     * 读线程条件
     */
    private final Condition empty = lock.newCondition();
    /**
     * 写线程条件
     */
    private final Condition full = lock.newCondition();
    /**
     * 缓存队列
     */
    private ArrayDeque<T> items;
    private int maxSize;

    public CacheUtil(int size) {
        this.maxSize = size;
        this.items = new ArrayDeque<>(size);
    }

    public void put(T t) throws InterruptedException {
        lock.lock();
        try {
            while (isFull()){
                full.await();
            }
            items.add(t);
            if(isFull()){
                empty.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public ArrayDeque<T> takeAll() throws InterruptedException {
        ArrayDeque<T> result = new ArrayDeque<>(maxSize);
        lock.lock();
        try {
            while (!isFull())
                empty.await();
            for(T t : items){
                result.add(t);
            }
            clear();
            full.signal();
            return result;
        } finally {
            lock.unlock();
        }
    }

    private boolean isFull() {
        return (items.size() == maxSize);
    }

    private boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}
