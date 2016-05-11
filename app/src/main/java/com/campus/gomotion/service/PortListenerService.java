package com.campus.gomotion.service;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Author: zhong.zhou
 * Date: 16/4/23
 * Email: muxin_zg@163.com
 */
public class PortListenerService implements Callable<String> {
    private final static String TAG = "PortListenerService";
    /**
     * the port of service
     */
    private int port;

    private Handler handler;

    /**
     * the server socket of service
     */
    private ServerSocket serverSocket;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PortListenerService(int port, Handler handler) {
        this.port = port;
        this.handler = handler;
    }

    /**
     * close server socket interrupt the port listen service
     */
    public void closeServerSocket() {
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.v(TAG, "close server socket", e);
        }
    }

    @Override
    public String call() {
        Socket socket = null;
        SynchronizeService synchronizeService = null;
        FutureTask<String> futureTask = null;
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                socket = serverSocket.accept();
                synchronizeService = new SynchronizeService(socket, handler);
                futureTask = new FutureTask<>(synchronizeService);
                executorService.submit(futureTask);
                Log.v(TAG, "start synchronize service");
                Thread.sleep(5000);
            }
        } catch (IOException e) {
            Log.v(TAG, "listen service io exception", e);
        } catch (Exception e) {
            Log.v(TAG, "unexpected exception");
        } finally {
            try {
                if (executorService != null) {
                    executorService.shutdown();
                }
                if (serverSocket != null) {
                    serverSocket.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (futureTask != null) {
                    if (futureTask.cancel(true)) {
                        Log.v(TAG, "cancel synchronize service succeed");
                    } else {
                        Log.v(TAG, "cancel synchtonize service failed");
                    }
                }
            } catch (IOException e) {
                Log.v(TAG, "close resource io exception", e);
            }
        }
        Log.v(TAG, "port listener service finished");
        return "port listener service finished";
    }
}
