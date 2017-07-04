package com.tjstudy.tcpclientdemo;

import android.util.Log;

import com.tjstudy.tcplib.BaseRecParse;
import com.tjstudy.tcplib.utils.DigitalUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义转换规则
 * 自定义接收数据的协议
 * 示例：接收到的数据必须以$$(2个字节)开头 数据的大小（short,byte 2个字节 数据大小=2+2+数据大小）
 */

public class MyRecParse extends BaseRecParse<RecData> {
    private static final String TAG = "MyRecdataFilter";

    @Override
    public List<RecData> parse() {
        ArrayList<RecData> recDataList = new ArrayList<>();
        byte[] baseData = getBaseData();//总数据

        int removeSize = 0;
        byte[] head = new byte[2];

        for (int i = 0; i < baseData.length - 1; i++) {
            if (baseData[i] == '$' && baseData[i + 1] == '$') {
                //找到头了 进一步进行处理
                //然后两个字节是数据长度
                short dataLen = DigitalUtils.byte2Short(baseData, i + 2);
                if (baseData.length - i + 1 > dataLen) {//说明数据完整
                    byte[] recData = new byte[dataLen];
                    System.arraycopy(baseData, i + 4, recData, 0, dataLen - 4);
                    Log.e(TAG, "parse: 有完整的数据=" + new String(recData));
                    i += dataLen - 1;
                    removeSize += dataLen;

                    //头
                    head[0] = recData[0];
                    head[1] = recData[1];
                    //长度 dataLen
                    //数据 bytes
                    recDataList.add(new RecData(head, dataLen, recData));
                } else {
                    Log.e(TAG, "parse: nonono完整的数据");
                    break;//如果长度不足 就等着
                }
            } else {
                removeSize++;
            }
        }
        notifyLeftData(removeSize);
        return recDataList;
    }
}
