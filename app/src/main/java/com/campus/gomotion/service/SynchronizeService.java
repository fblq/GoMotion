package com.campus.gomotion.service;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.campus.gomotion.sensorData.Accelerometer;
import com.campus.gomotion.sensorData.AngularVelocity;
import com.campus.gomotion.sensorData.DataPack;
import com.campus.gomotion.util.CacheUtil;
import com.campus.gomotion.sensorData.Quaternion;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

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

    private String file = "/storage/emulated/0/amotion/data.txt";

    @Override
    public String call() {
        /**
         * 数据发送频率为20ms/次
         * 数据包包括12个float类型的数据,1s内接收的数据大小为4*12*50=2400
         */
        ByteBuffer byteBuffer = ByteBuffer.allocate(2400);
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        try {
            int i, len = 0;
            byte packHead = 0;
            int packCount = 0;
            float packContent;
            fileWriter = new FileWriter(file);
            printWriter = new PrintWriter(fileWriter);
            while (socketChannel.read(byteBuffer) != -1) {
                /**
                 * 准备读取数据
                 */
                byteBuffer.flip();
                for (i = 0; byteBuffer.hasRemaining(); i++) {
                    packHead = byteBuffer.order(ByteOrder.nativeOrder()).get();
                    if (packHead == 80) {
                        DataPack dataPack = new DataPack();
                        Quaternion quaternion = new Quaternion();
                        Accelerometer accelerometer = new Accelerometer();
                        AngularVelocity angularVelocity = new AngularVelocity();
                        if (byteBuffer.hasRemaining()) {
                            packCount = byteBuffer.order(ByteOrder.nativeOrder()).getInt();
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
                        printWriter.print(packHead+" ");
                        printWriter.print(packCount+" ");
                        printWriter.println(dataPack.toString());
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
                if (printWriter != null) {
                    printWriter.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
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
