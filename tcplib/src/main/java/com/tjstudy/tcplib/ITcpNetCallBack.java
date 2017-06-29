package com.tjstudy.tcplib;

/**
 * 服务器接口回调
 */
public abstract class ITcpNetCallBack {
    /**
     * 操作执行成功
     */
    public abstract void onSuccess();

    /**
     * 操作执行失败
     *
     * @param failMess 失败原因
     */
    public abstract void onFail(String failMess);

    /**
     * 操作超过了指定时间，后面任然可能接收到数据，在请求操作是设置了超时时间才会有效
     */
    protected void onTimeout() {
    }
}