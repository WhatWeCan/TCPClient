package com.tjstudy.tcplib;

import android.os.Handler;
import android.os.Message;

import com.tjstudy.tcplib.utils.LoopBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * 设置连接参数
 */

public class TCPClient {
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
    private int mBreathTime;
    private static TCPClient client;
    private int readTimeout;
    private RequestCallback requestCallback;
    private byte[] sendData;
    private long sendTime;
    private long recTime;
    private ResponseCallback responseCallback;

    private TCPClient() {
    }

    /**
     * 获取TCPClient的实例
     *
     * @return
     */
    public static TCPClient build() {
        if (client == null) {
            synchronized (TCPClient.class) {
                if (client == null) {
                    client = new TCPClient();
                }
            }
        }
        return client;
    }

    private static final int SOCKET_OK = 10003;
    private static final int SOCKET_CLOSED = 10004;
    private static final int RE_CONN_SUCCESS = 10005;
    private static final int RE_CONN_FAIL = 10006;
    private static final int ON_RECEIVE_DATA = 10007;
    private static final int ON_REQUEST_FAIL = 10008;
    private static final int ON_RECEIVE_DATA_TIMEOUT = 10009;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SOCKET_OK:
                    //发送数据
                    Socket socket = (Socket) msg.obj;
                    request(socket);
                    break;
                case SOCKET_CLOSED:
                    //尝试进行重新连接
                    connectServerInner();
                    break;
                case RE_CONN_SUCCESS:
                    //发送数据
                    socket = (Socket) msg.obj;
                    request(socket);
                    receive(socket);
                    sendBreadth(socket);
                    break;
                case ON_RECEIVE_DATA:
                    if (responseCallback != null) {
                        responseCallback.onRec();
                    }
                    break;
                case RE_CONN_FAIL:
                    Throwable connE = (Throwable) msg.obj;
                    throwBack(connE);
                    break;
                case ON_REQUEST_FAIL:
                    Throwable requestE = (Throwable) msg.obj;
                    throwBack(requestE);
                    break;

                case ON_RECEIVE_DATA_TIMEOUT:
                    if (requestCallback != null) {
                        requestCallback.onTimeout();
                    }
                    break;
            }
        }
    };

    /**
     * 设置目标服务器的ip和端口号
     *
     * @param ip   ip地址
     * @param port 端口号
     * @return
     */
    public TCPClient server(String ip, int port) {
        mServerIp = ip;
        mServerPort = port;
        return this;
    }

    /**
     * 设置连接超时时间
     *
     * @param connTimeout 连接超时时间
     * @return
     */
    public TCPClient connTimeout(int connTimeout) {
        mConnTimeout = connTimeout;
        return this;
    }

    /**
     * 设置心跳数据
     *
     * @param breath     心跳内容
     * @param breathTime 心跳间隔时间
     * @return
     */
    public TCPClient breath(byte[] breath, int breathTime) {
        mBreath = breath;
        mBreathTime = breathTime;
        return this;
    }

    //----------------------发送数据：判断连接是否存在；连接存在 则直接发送数据，连接不存在，尝试进行连接，连接成功发送数据

    /**
     * 发送请求到服务器
     *
     * @param data     数据
     * @param timeout  请求响应超时时间
     * @param request  请求状态
     * @param response 接收数据
     */
    public synchronized void request(byte[] data, int timeout, RequestCallback request, ResponseCallback response) {
        readTimeout = timeout;
        requestCallback = request;
        responseCallback = response;
        sendData = data;
        isServerOpen();
    }
    //----------------------接收数据：判断连接是否存在；连接存在 则不进行操作，连接不存在，尝试进行连接，连接成功接收数据

    /**
     * 接收数据
     *
     * @param responseCallBack 接收数据
     */
    public void onResponse(ResponseCallback responseCallBack) {
        this.responseCallback = responseCallBack;
        isServerOpen();
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

    /**
     * 关闭当前连接
     */
    public void closeTcp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = TCPClientManager.queryTarget(mServerIp, mServerPort);
                if (socket != null) {
                    TCPClientManager.removeTcp(socket);
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
                } catch (final IOException e) {
                    e.printStackTrace();
                    Message message = mHandler.obtainMessage();
                    message.what = RE_CONN_FAIL;
                    message.obj = e;
                    mHandler.sendMessage(message);

                }
            }
        }).start();
    }

    /**
     * 内部处理：发送心跳
     */
    private void sendBreadth(final Socket socket) {
        if (mBreath == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket != null && !socket.isClosed()) {
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

    /**
     * 内部处理：发送请求
     *
     * @param socket
     */
    private synchronized void request(final Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream os = socket.getOutputStream();
                    if (sendData != null) {
                        os.write(sendData);//发送请求
                        os.flush();
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                    Message message = mHandler.obtainMessage();
                    message.what = ON_REQUEST_FAIL;
                    message.obj = e;
                    mHandler.sendMessage(message);
                }
            }
        }).start();
        sendTime = System.currentTimeMillis();
        if (readTimeout != 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (recTime < sendTime) {
                        mHandler.sendEmptyMessage(ON_RECEIVE_DATA_TIMEOUT);
                    }
                }
            }, readTimeout + 1000);
        }
    }

    /**
     * 内部处理：接收数据
     *
     * @param socket
     */
    private void receive(final Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket.isConnected() && !socket.isClosed()) {
                    //接收socket数据
                    try {
                        InputStream is = socket.getInputStream();
                        byte[] data = new byte[1024];
                        int len = is.read(data);
                        byte[] newData = new byte[len];
                        System.arraycopy(data, 0, newData, 0, len);
                        LoopBuffer.getInstance().write(newData);
                        recTime = System.currentTimeMillis();
                        mHandler.sendEmptyMessage(ON_RECEIVE_DATA);
                    } catch (SocketTimeoutException ignored) {
                    } catch (SocketException e) {
                        if (socket.isClosed()) {
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 判断网络是否打开的操作
     */
    private void isServerOpen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //查看指定服务器是否已经连接
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

    private void throwBack(Throwable e) {
        if (responseCallback != null) {
            responseCallback.onFail(e);
        }
        if (requestCallback != null) {
            requestCallback.onFail(e);
        }
    }
}
