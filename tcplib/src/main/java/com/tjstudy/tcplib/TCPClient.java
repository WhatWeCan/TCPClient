package com.tjstudy.tcplib;

import android.content.Context;

import com.tjstudy.tcplib.utils.LoopBuffer;

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
    private static Context mContext;
    private static TCPClient client;

    private TCPClient() {
    }

    /**
     * 获取TCPClient的实例
     *
     * @return
     */
    public static TCPClient with(Context appContext) {
        mContext = appContext;
        if (client == null) {
            synchronized (TCPClient.class) {
                if (client == null) {
                    client = new TCPClient();
                }
            }
        }
        return client;
    }

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

    /**
     * 连接到服务器
     *
     * @param callBack
     */
    public TCPClient connect(ITcpNetCallBack callBack) {
        TCPClientOperation operation = new TCPClientOperation(this);
        operation.connectServer(callBack);
        return this;
    }

    /**
     * 发送数据
     *
     * @param data
     * @param callBack
     */
    public TCPClient sendData(byte[] data, int reqTimeout, ITcpNetCallBack callBack) {
        TCPClientOperation operation = new TCPClientOperation(this);
        operation.sendData(data, reqTimeout, callBack);
        return this;
    }

    /**
     * 关闭所有连接
     */
    public static void closeTcpAll() {
        TCPClientOperation.closeAll();
    }

    /**
     * 关闭指定连接
     *
     * @param ip
     * @param port
     */
    public static void closeTcp(String ip, int port) {
        TCPClientOperation.closeTcp(ip, port);
        LoopBuffer.getInstance().remove(LoopBuffer.getInstance().count());
    }

    //---------------getter 基本信息-----------
    public String getServerIp() {
        return mServerIp;
    }

    public int getServerPort() {
        return mServerPort;
    }

    public int getConnTimeout() {
        return mConnTimeout;
    }

    public byte[] getBreath() {
        return mBreath;
    }

    public int getBreathTime() {
        return mBreathTime;
    }

    public Context getContext() {
        return mContext;
    }
}
