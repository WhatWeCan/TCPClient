package com.tjstudy.tcplib;

import com.tjstudy.tcplib.utils.LoopBuffer;

import java.util.List;

/**
 * 默认接收数据处理方式：不进行处理 直接返回接收到的数据
 */
public abstract class BaseRecParse<T> {
    private final LoopBuffer instance;
    private final byte[] data;

    public BaseRecParse() {
        instance = LoopBuffer.getInstance();
        data = instance.read(instance.count());
    }

    public abstract List<T> parse();

    /**
     * 获取接收到的数据
     *
     * @return
     */
    public byte[] getBaseData() {
        return data;
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
