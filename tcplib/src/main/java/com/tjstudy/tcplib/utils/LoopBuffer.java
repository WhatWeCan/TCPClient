package com.tjstudy.tcplib.utils;

/**
 * loopBuffer
 */
public class LoopBuffer {
    private static int wp = 0;
    private static int rp = 0;
    private static int count = 0;
    private static int maxlen = 0;
    private static byte[] buff;
    private static LoopBuffer instance;

    private LoopBuffer() {
    }

    public static LoopBuffer getInstance() {
        if (instance == null) {
            synchronized (LoopBuffer.class) {
                if (instance == null) {
                    instance = new LoopBuffer(1024);
                }
            }
        }
        return instance;
    }

    private LoopBuffer(int len) {
        wp = 0;
        rp = 0;
        count = 0;
        maxlen = len;
        buff = new byte[maxlen];
    }

    public void write(byte[] data) {
        if (count + data.length > maxlen)
            return;

        int rlen = maxlen - wp;
        if (rlen > data.length) {
            System.arraycopy(data, 0, buff, wp, data.length);
            wp += data.length;
            if (wp >= maxlen) {
                wp %= maxlen;
            }
        } else {
            System.arraycopy(data, 0, buff, wp, rlen);
            System.arraycopy(data, rlen, buff, 0, data.length - rlen);
            wp = data.length - rlen;
        }
        count += data.length;
    }

    public byte[] read(int readLen) {
        if (readLen > count)
            readLen = count;

        byte[] rbuf = new byte[readLen];
        int rlen = maxlen - rp;
        if (readLen < rlen) {
            System.arraycopy(buff, rp, rbuf, 0, readLen);
        } else {
            int len1 = maxlen - rp;
            System.arraycopy(buff, rp, rbuf, 0, len1);
            System.arraycopy(buff, 0, rbuf, len1, readLen - len1);
        }

        return rbuf;
    }

    public int count() {
        return count;
    }

    public void remove(int len) {
        if (len > count)
            return;
        count -= len;
        rp += len;
        rp %= maxlen;
    }
}
