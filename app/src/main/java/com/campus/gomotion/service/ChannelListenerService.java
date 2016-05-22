package com.campus.gomotion.service;

import android.os.Handler;
import android.telecom.Call;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Author zhong.zhou
 * Date 5/21/16
 * Email qnarcup@gmail.com
 */
public class ChannelListenerService implements Callable<String> {
    private final static String TAG = "ChannelListenerService";
    /**
     * the port of service
     */
    private int port;

    private Handler handler;

    /**
     * the server socket of service
     */
    private ServerSocketChannel serverSocketChannel;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ChannelListenerService(int port, Handler handler) {
        this.port = port;
        this.handler = handler;
    }

    /**
     * close server socket interrupt the port listen service
     */
    public void closeServerSocket() {
        try {
            if (serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
        } catch (IOException e) {
            Log.v(TAG, "close server socket channel", e);
        }
    }

    @Override
    public String call() {
        SocketChannel socketChannel = null;
        SynchronizeService synchronizeService = null;
        FutureTask<String> futureTask = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            while (true) {
                socketChannel = serverSocketChannel.accept();
                synchronizeService = new SynchronizeService(socketChannel, handler);
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
                if (serverSocketChannel != null) {
                    serverSocketChannel.close();
                }
                if (socketChannel != null && socketChannel.isOpen()) {
                    socketChannel.close();
                }
                if (futureTask != null) {
                    if (futureTask.cancel(true)) {
                        Log.v(TAG, "cancel synchronize service succeed");
                    } else {
                        Log.v(TAG, "cancel synchronize service failed");
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
