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
    compile 'com.github.WhatWeCan:TCPClient:v1.0'
}
```

2、使用方法
------

**连接到指定服务器**
```
		TCPClient.with(appContext)
                .server("192.168.5.75", 8888)
                .connect(new ITcpNetCallBack() {
                    @Override
                    public void onSuccess() {
                       //操作执行成功
                    }

                    @Override
                    public void onFail(String failMess) {
                       //操作执行失败 
                    }
                });
```
**连接到服务器，并设置心跳**
心跳必须在连接时进行设置

```
		TCPClient.with(appContext)
                .server("192.168.5.75", 8888)
                .breath("heart...".getBytes(), 10 * 1000)
                .connect(new ITcpNetCallBack() {
                    @Override
                    public void onSuccess() {
                        //操作执行成功
                    }

                    @Override
                    public void onFail(String failMess) {
                        //操作执行失败
                    }
                });
```

**发送数据**
如果客户端已经close了，再发送数据时，会自动重连，连接时设置有心跳的 也会自动再发送心跳

```
		TCPClient.with(appContext)
                .server("192.168.5.75", 8888)
                .sendData(etSendData.getText().toString().getBytes(), 8000, new ITcpNetCallBack() {
                    @Override
                    public void onSuccess() {
                        //操作执行成功
                    }

                    @Override
                    public void onFail(String failMess) {
                       //发送数据失败
                    }

                    @Override
                    protected void onTimeout() {
                        super.onTimeout();
                        //读取数据超时
                    }
                });
```

接收数据
----

使用广播接收者
需要接收的广播的**action=服务器的ip地址:端口号**
示例：192.168.5.75：8888

**广播接收者示例**：

```
class RecTCPBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            BaseRecParse myParse = new BaseRecParse();
            List<byte[]> parse = myParse.parse();
            if (parse.size() > 0) {
                for (byte[] data :parse) {
                    Log.e(TAG, "onReceive: 接收到的数据-" + new String(data));
                }
            } else {
                Log.e(TAG, "onReceive: 没有数据符合规则");
            }
        }
    }
```

BaseRecParse用于规范接收数据的格式，BasRecParse并没有写入规范，接收到的数据是什么就取出来什么。

**支持自定义接收数据的格式**
示例：

```
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
```

```
//获取指定数据格式下的返回数据
BaseRecParse myParse = new MyBaseRecParse();
List<byte[]> parse = myParse.parse();
```

关闭连接
----

关闭指定连接
TCPClient.closeTcp(“192.168.5.75”, 8888);
关闭所有连接
TCPClient.closeTcpAll();

demo 演示
-------

![这里写图片描述](http://img.blog.csdn.net/20170629160503721?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMjM5MTg3Ng==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

服务器端截图：略

demo地址
------

app：https://github.com/WhatWeCan/TCPClientTest
server:https://github.com/WhatWeCan/TCPServerDemo
