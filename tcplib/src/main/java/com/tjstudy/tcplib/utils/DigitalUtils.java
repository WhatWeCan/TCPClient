package com.tjstudy.tcplib.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * 数字工具
 *
 * @author Administrator
 */
public class DigitalUtils {

    /**
     * 将字符串转换为byte数组
     *
     * @param str
     * @param length
     * @return
     */
    public static void packMessageString(String str, int length, byte[] buff, int[] start) {
        try {
            byte[] strByte = str.getBytes("GBK");
            int realLen = strByte.length;
            if (realLen > length)
                realLen = length;
            System.arraycopy(strByte, 0, buff, start[0], realLen);
            start[0] += length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void packMessageInt(int data, byte[] buff, int[] start) {
        byte[] d = int2Byte(data);
        System.arraycopy(d, 0, buff, start[0], 4);
        start[0] += 4;
    }

    public static void packMessageShort(short data, byte[] buff, int[] start) {
        byte[] d = short2Byte(data);
        System.arraycopy(d, 0, buff, start[0], 2);
        start[0] += 2;
    }

    public static void packMessageByte(byte data, byte[] buff, int[] start) {
        buff[start[0]] = data;
        start[0] += 1;
    }

    public static String parseMessageString(byte[] buf, int[] start, int count) {
        byte[] result = new byte[count];
        System.arraycopy(buf, start[0], result, 0, count);
        start[0] += count;
        return byte2String(result).trim();
    }

    public static int parseMessageInt(byte[] buf, int[] start) {
        if(buf.length<4){
            return -1;
        }

        byte[] result = new byte[4];
        System.arraycopy(buf, start[0], result, 0, 4);
        start[0] += 4;

        String s = "";
        for (byte b :
                result) {
            s += b + ",";
        }
        return byte2Int(result);
    }

    public static int parseMessageShort(byte[] buf, int[] start) {
        if(buf.length<2){
            return -1;
        }
        byte[] result = new byte[2];
        System.arraycopy(buf, start[0], result, 0, 2);
        start[0] += 2;
        return byte2Short(result);
    }

    public static byte parseMessageByte(byte[] buf, int[] start) {
        byte b = buf[start[0]];
        start[0] += 1;
        return b;
    }

    /**
     * 将长度为4的byte数组转换为16位int
     * 大端字序
     *
     * @param res byte[]
     * @return int
     */
    private static int byte2Int(byte[] res) {
        return (res[0] & 0xff) | ((res[1] << 8) & 0xff00)
                | ((res[2] << 16) & 0xff0000) | ((res[3] << 24) & 0xff000000); // 表示安位或
    }

    /**
     * 将int 转为四个字节的byte
     *
     * @param i
     * @return
     */
    private static byte[] int2Byte(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) (i & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[2] = (byte) ((i >> 16) & 0xFF);
        result[3] = (byte) ((i >> 24) & 0xFF);
        return result;
    }

    /**
     * 将byte数组转换为字符串
     *
     * @param src
     * @return
     */
    private static String byte2String(byte[] src) {
        String gbk = "";
        try {
            gbk = new String(src, "gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return gbk;
    }

    /**
     * 将short转换为byte数组
     *
     * @param s short
     * @return
     */
    private static byte[] short2Byte(int s) {
        byte[] shortBuf = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (shortBuf.length - 1 - i) * 8;
            shortBuf[1 - i] = (byte) ((s >>> offset) & 0xff);
        }
        return shortBuf;
    }

    /**
     * 将长度为2的byte数组转换为16位int
     *
     * @param res byte[]
     * @return int
     */
    private static int byte2Short(byte[] res) {
        return (res[0] & 0xff) | ((res[1] << 8) & 0xff00); // | 表示安位或
    }

    /**
     * 将长度为2的byte数组转换为16位int
     *
     * @param res byte[]
     * @return int
     */
    public static short byte2Short(byte[] res, int start) {
        int targets = (res[start] & 0xff) | ((res[start + 1] << 8) & 0xff00); // | 表示安位或
        return (short) targets;
    }

    /**
     * 根据char数组获取byte数组
     *
     * @param chars
     * @return
     */
    public static byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("gbk");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);

        return bb.array();
    }

    /**
     * 根据byte数组获取char数组
     *
     * @param bytes
     * @return
     */
    public static char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("gbk");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);

        return cb.array();
    }

    /**
     * @param msg   需要计算校验和的byte数组(无符号校验和)
     * @param start msg的开始位置
     * @return 计算出的校验和数组
     */
    public static short sumCheckShort(byte[] msg, int start, int count) {
        byte[] newByte = new byte[count];
        System.arraycopy(msg, start, newByte, 0, newByte.length);
        int mSum = 0;
        int temp;
        short chksum;
        int i = 0;
        for (; i < newByte.length - 1; i += 2) {
            temp = newByte[i + 1];
            temp <<= 8;
            if (temp < 0) {
                temp += 65536;
            }
            if (newByte[i] < 0) {
                temp += 256;
            }
            temp += newByte[i];
            mSum += temp;
        }
        if (i != newByte.length) {
            if (newByte[newByte.length - 1] < 0) {
                mSum += 256;
            }
            mSum += newByte[newByte.length - 1];
        }
        mSum = (mSum >> 16) + (mSum & 0xffff);
        mSum += (mSum >> 16);
        chksum = (short) ~mSum;
        return chksum;
    }

    /**
     * 组合byte数组
     *
     * @param bs
     * @return
     */
    public static byte[] mergeBytes(byte[]... bs) {
        int length = 0;
        for (byte[] bs2 : bs) {
            length += bs2.length;
        }
        // 请求数组长度
        byte[] result = new byte[length];
        int curLength = 0;
        for (byte[] b : bs) {
            System.arraycopy(b, 0, result, curLength, b.length);
            curLength += b.length;
        }
        return result;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv + " ");
        }
        return stringBuilder.toString();
    }
}
