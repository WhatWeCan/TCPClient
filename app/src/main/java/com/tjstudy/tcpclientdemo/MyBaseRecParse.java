package com.tjstudy.tcpclientdemo;

import android.util.Log;

import com.tjstudy.tcplib.utils.DigitalUtils;
import com.tjstudy.tcplib.BaseRecParse;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * 自定义处理方式
 */

public class MyBaseRecParse extends BaseRecParse {
    @Override
    public List<byte[]> parse() {
        ArrayList<byte[]> dataList = new ArrayList<>();
        //待处理数据 data:byte[]
        //自定义接收数据的协议
        //示例：接收到的数据必须以$$(2个字节)开头 数据的大小（short,byte 2个字节 数据大小=2+2+数据大小）
        int removeSize = 0;

        for (int i = 0; i < data.length - 1; i++) {
            if (data[i] == '$' && data[i + 1] == '$') {
                //找到头了 进一步进行处理
                //然后两个字节是数据长度
                short dataLen = DigitalUtils.byte2Short(data, i + 2);
                Log.e(TAG, "parse: dataLen=" + data.length);
                Log.e(TAG, "parse: recLen=" + dataLen);
                if (data.length - i + 1 > dataLen) {//说明数据完整
                    Log.e(TAG, "parse: 有完整的数据");
                    byte[] bytes = new byte[dataLen];
                    System.arraycopy(data, i, bytes, 0, dataLen);
                    i += dataLen - 1;
                    dataList.add(bytes);
                    removeSize += dataLen;
                } else {
                    Log.e(TAG, "parse: nonono完整的数据");
                    break;//如果长度不足 就等着
                }
            } else {
                removeSize++;
            }
        }
        notifyLeftData(removeSize);
        return dataList;
    }
}
