package com.tjstudy.tcplib;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import com.tjstudy.tcplib.utils.LoopBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * 网络访问具体操作类
 */

public class TCPClientOperation {
    private static final int SOCKET_OK = 10003;
    private static final int SOCKET_CLOSED = 10004;
    private static final int RE_CONN_SUCCESS = 10005;
    TCPClient client;
    /**
     * 服务器ip
     */
    private String mServerIp;
    /**
     * 服务器端口号
     */
    private int mServerPort;
    /**
     * 连接超时时间 默认设置为10s
     */
    private int mConnTimeout = 10 * 1000;
    /**
     * 心跳数据
     */
    private byte[] mBreath;
    /**
     * 心跳间隔时间
     */
    private long mBreathTime;
    private ITcpNetCallBack sendDataCallBack;
    private final Context appContext;
    private long sendTime;
    private static long recTime;
    private static byte[] sendData;
    private static boolean isCloseAll = false;
    private int readTimeout;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SOCKET_OK:
                    //发送数据
                    Socket socket = (Socket) msg.obj;
                    sendData(socket);
                    break;
                case SOCKET_CLOSED:
                    //尝试进行重新连接
                    connectServerInner();
                    break;
                case RE_CONN_SUCCESS:
                    //发送数据
                    socket = (Socket) msg.obj;
                    sendData(socket);
                    receive(socket);
                    sendBreadth(socket);
                    break;
            }
        }
    };

    public TCPClientOperation(TCPClient client) {
        this.client = client;
        //1、获取基本数据信息
        mServerIp = client.getServerIp();
        mServerPort = client.getServerPort();
        mConnTimeout = client.getConnTimeout();
        mBreath = client.getBreath();
        mBreathTime = client.getBreathTime();
        appContext = client.getContext();
    }

    /**
     * 连接到服务器，如果已经连接 则从列表中删除连接 在进行连接
     *
     * @param connCallBack 连接反馈
     */
    public void connectServer(final ITcpNetCallBack connCallBack) {
        //建立新的连接
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //判断指定网络是否在列表中
                    Socket socket = TCPClientManager.queryTarget(mServerIp, mServerPort);
                    if (socket == null) {//在列表中存在，则直接进行连接
                        socket = new Socket();
                        socket.setSoTimeout(10 * 1000);//设置读取超时时间
                        socket.connect(new InetSocketAddress(mServerIp, mServerPort), mConnTimeout);
                        //连接服务器成功
                        TCPClientManager.addTCP(socket);//添加到列表
                    }
                    connCallBack.onSuccess();
                    receive(socket);
                    sendBreadth(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                    //连接失败
                    connCallBack.onFail(e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 开启线程 发送心跳
     */
    private void sendBreadth(final Socket socket) {
        if (mBreath == null) {
            return;
        }
        isCloseAll = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isCloseAll && socket != null && !socket.isClosed()) {
                    try {
                        OutputStream os = socket.getOutputStream();
                        os.write(mBreath);//发送心跳
                        os.flush();
                        Thread.sleep(mBreathTime);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        if (socket.isClosed()) {
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    //----------------------发送数据：判断连接是否存在；连接存在 则直接发送数据，连接不存在，尝试进行连接，连接成功发送数据

    /**
     * 发送数据到服务器
     *
     * @param data
     */
    public synchronized void sendData(final byte[] data, final int timeout, final ITcpNetCallBack callBack) {
        readTimeout = timeout;
        sendDataCallBack = callBack;
        sendData = data;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = TCPClientManager.queryTarget(mServerIp, mServerPort);
                if (socket != null) {
                    Message message = mHandler.obtainMessage();
                    message.what = SOCKET_OK;
                    message.obj = socket;
                    mHandler.sendMessage(message);
                } else {
                    Message message = mHandler.obtainMessage();
                    message.what = SOCKET_CLOSED;
                    mHandler.sendMessage(message);
                }
            }
        }).start();
    }

    /**
     * 开启线程连接服务器
     */
    private void connectServerInner() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    socket.setSoTimeout(10 * 1000);//设置读取超时时间
                    socket.connect(new InetSocketAddress(mServerIp, mServerPort), mConnTimeout);
                    //连接服务器成功
                    TCPClientManager.addTCP(socket);//添加到列表
                    Message message = mHandler.obtainMessage();
                    message.what = RE_CONN_SUCCESS;
                    message.obj = socket;
                    mHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    //连接失败
                    sendDataCallBack.onFail(e.getMessage());
                }
            }
        }).start();
    }

    private synchronized void sendData(final Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream os = socket.getOutputStream();
                    os.write(sendData);//发送数据
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    sendDataCallBack.onFail(e.getMessage());
                }
            }
        }).start();
        sendTime = System.currentTimeMillis();
        if (readTimeout != 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (recTime < sendTime) {
                        sendDataCallBack.onTimeout();
                    }
                }
            }, readTimeout + 1000);
        }
    }
    //--------------------------发送数据结束----------------------------------------

    public void receive(final Socket socket) {
        isCloseAll = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isCloseAll && socket.isConnected()) {
                    //接收socket数据
                    try {
                        InputStream is = socket.getInputStream();
                        byte[] data = new byte[1024];
                        int len = is.read(data);
                        byte[] newData = new byte[len];
                        System.arraycopy(data, 0, newData, 0, len);
                        LoopBuffer.getInstance().write(newData);
                        recTime = System.currentTimeMillis();

                        //发送本地广播 提示接收到了数据
                        LocalBroadcastManager instance = LocalBroadcastManager.getInstance(appContext);
                        Intent intent = new Intent();
                        intent.setAction(socket.getInetAddress().getHostName() + ":" + socket.getPort());
                        instance.sendBroadcast(intent);
                    } catch (SocketTimeoutException ignored) {
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (socket.isClosed()) {
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 关闭所有
     */
    public static void closeAll() {
        isCloseAll = true;
        LoopBuffer instance = LoopBuffer.getInstance();
        instance.remove(instance.count());
        TCPClientManager.closeAll();
    }

    /**
     * 关闭指定连接
     *
     * @param ip
     * @param port
     */
    public static void closeTcp(final String ip, final int port) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = TCPClientManager.queryTarget(ip, port);
                if (socket != null) {
                    TCPClientManager.removeTcp(socket);
                }
            }
        }).start();
    }
}