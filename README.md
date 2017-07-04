TCPClient
---------

  [![](https://jitpack.io/v/WhatWeCan/TCPClient.svg)](https://jitpack.io/#WhatWeCan/TCPClient)

----------
TCPClient android端的TCP封装

> 最近手上的一个项目，使用TCP进行数据的访问，根据目前的学习状态做一个简单的封装。
> github地址：https://github.com/WhatWeCan/TCPClient

怎么使用？
-----

1、导入
----

**Step 1**. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

```
allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```
**Step 2**. Add the dependency

```
	dependencies {
	        compile 'com.github.WhatWeCan:TCPClient:v1.0.2'
	}

```

2、使用方法
------

接收数据
----

接收数据：判断连接是否存在；连接存在 则不进行操作（默认连接存在就一直接收数据），连接不存在，尝试进行重连操作，连接成功接收数据

**1、自定义接收数据处理方式**

自定义接收数据类型

```
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
    //省略 getter setter
}
```

自定义接收数据规则 示例
```
/**
 * 自定义转换规则
 * 自定义接收数据的协议
 * 示例：接收到的数据必须以$$(2个字节)开头 数据的大小（short,byte 2个字节 数据大小=2+2+数据大小）
 */

public class MyRecParse extends BaseRecParse<RecData> {
    private static final String TAG = "MyRecParse";

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
                    Log.e(TAG, "parse: 有完整的数据");
                    byte[] recData = new byte[dataLen];
                    System.arraycopy(baseData, i, recData, 0, dataLen);
                    i += dataLen - 1;
                    removeSize += dataLen;

                    //头
                    head[0] = baseData[i];
                    head[1] = baseData[i + 1];
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
```
**2、开启接收数据**

```
TCPClient.build()
          .server(ipAddress, 8888)
          .breath("heart".getBytes(), 6 * 1000)
          .connTimeout(10 * 1000)
          .onResponse(responseCallback);
```

发送数据
----
发送数据：判断连接是否存在；连接存在 则直接发送数据，连接不存在，尝试进行重连操作，连接成功发送数据，并接收数据。

```
TCPClient.build()
        .server(ipAddress, 8888)
        .breath("heart".getBytes(), 6 * 1000)
        .connTimeout(10 * 1000)
        .request(sendMess.getBytes(), 8000, new RequestCallback() {
            @Override
            public void onTimeout() {
                Log.e(TAG, "onTimeout:请求超时，稍后重试 ,关闭连接 ");
                TCPClient.closeTcp(ipAddress, 8888);
            }

            @Override
            public void onFail(Throwable throwable) {
                handlerError(throwable);
            }
        }, responseCallback);//接收数据回调
```
关闭连接
----

关闭指定连接
TCPClient.closeTcp(“192.168.5.75”, 8888);

简易聊天demo 演示
-------

![这里写图片描述](http://img.blog.csdn.net/20170704120552841?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMjM5MTg3Ng==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

服务器端截图：略

demo地址
------

app：https://github.com/WhatWeCan/TCPClient
server:https://github.com/WhatWeCan/TCPServerDemo
