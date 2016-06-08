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
         * 数据包包括12个float类型的数据,1s内接收的数据大小为(4*10+6)*50=2300
         */
        ByteBuffer byteBuffer = ByteBuffer.allocate(2300);
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        boolean notLosing = true;
        long count = 0;
        try {
            String hexStr = "0123456789ABCDEF";
            StringBuilder stringBuilder = new StringBuilder();
            int i, j;
            byte a, b, temp;
            String packHead = "", packCount = "";
            float packContent;
            fileWriter = new FileWriter(file);
            printWriter = new PrintWriter(fileWriter);
            while (socketChannel.read(byteBuffer) != -1) {
                /**
                 * 准备读取数据
                 */
                byteBuffer.flip();
                for (i = 0; byteBuffer.hasRemaining(); i++) {
                    a = byteBuffer.order(ByteOrder.BIG_ENDIAN).get();
                    b = byteBuffer.order(ByteOrder.BIG_ENDIAN).get();
                    packHead = stringBuilder.append(hexStr.charAt(a >> 4 & 0x0f))
                            .append(hexStr.charAt(a & 0x0f)).append("-")
                            .append(hexStr.charAt(b >> 4 & 0x0f))
                            .append(hexStr.charAt(b & 0x0f)).toString();
                    stringBuilder.delete(0, stringBuilder.length());
                    for (j = 0; j < 4; j++) {
                        temp = byteBuffer.order(ByteOrder.BIG_ENDIAN).get();
                        packCount = stringBuilder.append(hexStr.charAt(temp >> 4 & 0x0f))
                                .append(hexStr.charAt(temp & 0x0f)).toString();
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                    if (packHead.equals("80-0A")) {
                        DataPack dataPack = new DataPack();
                        Quaternion quaternion = new Quaternion();
                        Accelerometer accelerometer = new Accelerometer();
                        AngularVelocity angularVelocity = new AngularVelocity();
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
                        printWriter.print(packHead + " ");
                        printWriter.print(packCount + " ");
                        printWriter.println(dataPack.toString());
                        notLosing = (count == Long.parseLong(packCount, 16));
                        Message message = handler.obtainMessage();
                        message.what = 0x12;
                        message.obj = notLosing;
                        handler.sendMessage(message);
                        count++;
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

    public static void main(String[] args) {
        String str = "00000001";
        long l = Long.parseLong(str, 16);
        System.out.println(l);
    }
}