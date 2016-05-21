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
    private Socket socket;
    /**
     * handle message
     */
    private Handler handler;
    /**
     * the input stream of socket
     */
    private DataInputStream inputStream;
    /**
     * the buffer for receiving data
     */
    private byte[] buffer = new byte[1300];

    private static String file = "/storage/emulated/0/amotion/data.txt";

    public SynchronizeService(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public String call() {
        String hexStr = "0123456789ABCDEF";
        StringBuilder stringBuilder = new StringBuilder();
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        try {
            fileWriter = new FileWriter(file);
            printWriter = new PrintWriter(fileWriter);
            inputStream = new DataInputStream(socket.getInputStream());
            while (inputStream.read(buffer, 0, 1300) != -1) {
                for (int i = 0; i < 50; i++) {
                    DataPack dataPack = new DataPack();
                    Quaternion quaternion = new Quaternion();
                    Accelerometer accelerometer = new Accelerometer();
                    AngularVelocity angularVelocity = new AngularVelocity();
                    String dataTag = stringBuilder.append(hexStr.charAt(buffer[i * 26] >> 4 & 0x0f))
                            .append(hexStr.charAt(buffer[i * 26] & 0x0f)).append("-")
                            .append(hexStr.charAt(buffer[1 + i * 26] >> 4 & 0x0f))
                            .append(hexStr.charAt(buffer[1 + i * 26] & 0x0f)).toString();
                    stringBuilder.delete(0, stringBuilder.length());
                    String dataCount = stringBuilder.append(hexStr.charAt(buffer[2 + i * 26] >> 4 & 0x0f))
                            .append(hexStr.charAt(buffer[2 + i * 26] & 0x0f)).
                                    append(hexStr.charAt(buffer[3 + i * 26] >> 4 & 0x0f))
                            .append(hexStr.charAt(buffer[3 + i * 26] & 0x0f)).
                                    append(hexStr.charAt(buffer[4 + i * 26] >> 4 & 0x0f))
                            .append(hexStr.charAt(buffer[4 + i * 26] & 0x0f)).
                                    append(hexStr.charAt(buffer[5 + i * 26] >> 4 & 0x0f))
                            .append(hexStr.charAt(buffer[5 + i * 26] & 0x0f)).toString();
                    stringBuilder.delete(0, stringBuilder.length());
                    if (dataTag.equals("80-0A")) {
                        quaternion.setW(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(buffer[6 + i * 26], buffer[7 + i * 26])))
                                .setX(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(buffer[8 + i * 26], buffer[9 + i * 26])))
                                .setY(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(buffer[10 + i * 26], buffer[11 + i * 26])))
                                .setZ(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(buffer[12 + i * 26], buffer[13 + i * 26])));
                        accelerometer.setX(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(buffer[14 + i * 26], buffer[15 + i * 26])) * 2)
                                .setY(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(buffer[16 + i * 26], buffer[17 + i * 26])) * 2)
                                .setZ(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(buffer[18 + i * 26], buffer[19 + i * 26])) * 2);
                        angularVelocity.setX(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(buffer[20 + i * 26], buffer[21 + i * 26])) * 2000)
                                .setY(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(buffer[22 + i * 26], buffer[23 + i * 26])) * 2000)
                                .setZ(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(buffer[24 + i * 26], buffer[25 + i * 26])) * 2000);
                        dataPack.setQuaternion(quaternion).setAccelerometer(accelerometer).setAngularVelocity(angularVelocity);
                        dataPacks.put(dataPack);
                        Message message = handler.obtainMessage();
                        message.what = 0x12;
                        message.obj = dataCount;
                        handler.sendMessage(message);
                        printWriter.print(buffer[i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[1 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[2 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[3 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[4 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[5 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[6 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[7 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[8 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[9 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[10 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[11 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[12 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[13 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[14 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[15 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[16 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[17 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[18 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[19 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[20 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[21 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[22 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[23 + i * 26]);
                        printWriter.print(" ");
                        printWriter.print(buffer[24 + i * 26]);
                        printWriter.print(" ");
                        printWriter.println(buffer[25 + i * 26]);
                    }
                }
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
                if (inputStream != null) {
                    /**
                     * 省略接收缓存区的残留数据
                     */
                    int length = inputStream.available();
                    inputStream.skipBytes(length);
                    inputStream.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                Log.v(TAG, "close resource", e);
            }
        }
        Log.v(TAG, "synchronization service finished");
        return "synchronization service finished";
    }
}
