package com.tjstudy.tcpclientdemo;

/**
 * 接收数据的类型
 */

public class RecData {
    private byte[] head;
    private short len;
    private byte[] data;

    public RecData() {
    }

    public RecData(byte[] head, short len, byte[] data) {
        this.head = head;
        this.len = len;
        this.data = data;
    }

    public byte[] getHead() {
        return head;
    }

    public void setHead(byte[] head) {
        this.head = head;
    }

    public short getLen() {
        return len;
    }

    public void setLen(short len) {
        this.len = len;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
