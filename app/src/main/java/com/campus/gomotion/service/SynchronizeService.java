package com.campus.gomotion.service;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.campus.gomotion.util.Cache;
import com.campus.gomotion.sensorData.Quaternion;
import com.campus.gomotion.util.BasicConversionUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Author: zhong.zhou
 * Date: 16/4/21
 * Email: muxin_zg@163.com
 */
public class SynchronizeService implements Callable<String> {
    private static final String TAG = "SynchronizeService";
    public static Cache<Quaternion> minuteCache = new Cache<>(50);
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

    public SynchronizeService(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public String call() {
        String hexStr = "0123456789ABCDEF";
        Quaternion quaternion = new Quaternion();
        //StringBuilder stringBuilder = new StringBuilder();
        byte[] data = new byte[16];
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            while (inputStream.read(data, 0, 16) != -1) {
                quaternion.setW(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(data[4],data[5])));
                quaternion.setX(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(data[6],data[7])));
                quaternion.setY(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(data[8],data[9])));
                quaternion.setZ(BasicConversionUtil.fixedToFloat(BasicConversionUtil.combine(data[10],data[11])));
                minuteCache.put(quaternion);
               /* for (int i = 0; i < 16; i++) {
                    stringBuilder.append("-").append(hexStr.charAt(data[i]>>4 & 0x0f)).append(hexStr.charAt(data[i] & 0x0f));
                }*/
                Message message = handler.obtainMessage();
                message.what = 0x12;
                message.obj = quaternion.toString();
                handler.sendMessage(message);
                //stringBuilder.delete(0, stringBuilder.length());
                Thread.sleep(20);
            }
        } catch (IOException e) {
            Log.v(TAG, "synchronize data io exception", e);
        } catch (Exception e) {
            Log.v(TAG, "unexpected exception");
        } finally {
            try {
                if (inputStream != null) {
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
