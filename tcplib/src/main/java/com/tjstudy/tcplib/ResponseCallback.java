package com.tjstudy.tcplib;

/**
 * 读取数据返回接口
 */

public abstract class ResponseCallback implements AbsBaseCallback {
    public abstract void onRec();
}