package com.campus.gomotion.service;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.campus.gomotion.sensorData.Accelerometer;
import com.campus.gomotion.sensorData.AngularVelocity;
import com.campus.gomotion.sensorData.DataPack;
import com.campus.gomotion.util.CacheUtil;
import com.campus.gomotion.util.CircularQueueUtil;
import com.campus.gomotion.sensorData.Quaternion;
import com.campus.gomotion.util.BasicConversionUtil;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author: zhong.zhou
 * Date: 16/4/26
 * Email: muxin_zg@163.com
 */
public class SynchronizeService implements Callable<String> {
    private static final String TAG = "SynchronizeService";
    public static CacheUtil<DataPack> dataPacks = new CacheUtil<>(50);
    /**
     * the socket of service
     */
    private SocketChannel socketChannel;
    /**
     * handle message
     */
    private Handler handler;

    public SynchronizeService(SocketChannel socketChannel, Handler handler) {
        this.socketChannel = socketChannel;
        this.handler = handler;
    }

    @Override
    public String call() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(2400);
        try {
            int i, len = 0;
            double packHead = 0, packCount = 0;
            float packContent;
            while (socketChannel.read(byteBuffer) != -1) {
                /**
                 * 准备读取数据
                 */
                byteBuffer.flip();
                for (i = 0; byteBuffer.hasRemaining(); i++) {
                    packHead = byteBuffer.order(ByteOrder.nativeOrder()).getFloat();
                    if (packHead == 110110) {
                        DataPack dataPack = new DataPack();
                        Quaternion quaternion = new Quaternion();
                        Accelerometer accelerometer = new Accelerometer();
                        AngularVelocity angularVelocity = new AngularVelocity();
                        if (byteBuffer.hasRemaining()) {
                            packCount = byteBuffer.order(ByteOrder.nativeOrder()).getFloat();
                        }
                        for (i = 0; i < 10; i++) {
                            if (byteBuffer.hasRemaining()) {
                                packContent = byteBuffer.order(ByteOrder.nativeOrder()).getFloat();
                                switch (i) {
                                    case 0:
                                        quaternion.setW(packContent);
                                        break;
                                    case 1:
                                        quaternion.setX(packContent);
                                        break;
                                    case 2:
                                        quaternion.setY(packContent);
                                        break;
                                    case 3:
                                        quaternion.setZ(packContent);
                                        break;
                                    case 4:
                                        accelerometer.setX(packContent);
                                        break;
                                    case 5:
                                        accelerometer.setY(packContent);
                                        break;
                                    case 6:
                                        accelerometer.setZ(packContent);
                                        break;
                                    case 7:
                                        angularVelocity.setX(packContent);
                                        break;
                                    case 8:
                                        angularVelocity.setY(packContent);
                                        break;
                                    case 9:
                                        angularVelocity.setZ(packContent);
                                        break;
                                }
                            }
                        }
                        dataPack.setQuaternion(quaternion).setAccelerometer(accelerometer).setAngularVelocity(angularVelocity);
                        dataPacks.put(dataPack);
                        Message message = handler.obtainMessage();
                        message.what = 0x12;
                        message.obj = packCount;
                        handler.sendMessage(message);
                    }
                }
                /**
                 * 清空缓存,准备写入数据
                 */
                byteBuffer.clear();
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            Log.v(TAG, "synchronize data io exception", e);
        } catch (Exception e) {
            Log.v(TAG, "unexpected exception");
        } finally {
            try {
                if (socketChannel != null) {
                    socketChannel.close();
                }
                if (socketChannel != null) {
                    socketChannel.close();
                }
            } catch (IOException e) {
                Log.v(TAG, "close resource", e);
            }
        }

        Log.v(TAG, "synchronization service finished");
        return "synchronization service finished";
    }
}
