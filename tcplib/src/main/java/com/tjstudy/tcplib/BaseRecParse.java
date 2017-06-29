package com.tjstudy.tcplib;

import com.tjstudy.tcplib.utils.LoopBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认接收数据处理方式：不进行处理 直接返回接收到的数据
 */

public class BaseRecParse {
    private LoopBuffer instance = LoopBuffer.getInstance();
    public byte[] data = instance.read(instance.count());

    public List<byte[]> parse() {
        List<byte[]> dataList = new ArrayList<>();
        dataList.add(data);
        notifyLeftData(data.length);
        return dataList;
    }

    /**
     * 更新剩下的数据
     *
     * @param outSize 处理数据长度
     */
    public void notifyLeftData(int outSize) {
        instance.remove(outSize);
    }
}
