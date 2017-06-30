package com.tjstudy.tcpclientdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.tjstudy.tcplib.ITcpNetCallBack;
import com.tjstudy.tcplib.TCPClient;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecTCPBroadcast recTCPBroadcast;
    private EditText etSendData;
    private String ipAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipAddress = "192.168.5.75";

        recTCPBroadcast = new RecTCPBroadcast();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(recTCPBroadcast, new IntentFilter(ipAddress + ":8888"));

        etSendData = (EditText) findViewById(R.id.et_send_content);
    }

    /**
     * 点击事件：没有设置心跳 直接连接服务器
     *
     * @param view
     */
    public void onConnWithoutBreath(View view) {
        TCPClient.with(this)
                .server(ipAddress, 8888)
                .connect(new ITcpNetCallBack() {
                    @Override
                    public void onSuccess() {
                        Log.e(TAG, "onSuccess: withoutBreath");
                    }

                    @Override
                    public void onFail(String failMess) {
                        Log.e(TAG, "onFail: withoutBreath=" + failMess);
                    }
                });
    }

    /**
     * 点击事件：关闭withoutBreath的TCP连接
     *
     * @param view
     */
    public void onConnWithoutBreathClose(View view) {
        TCPClient.closeTcp(ipAddress, 8888);
    }

    public void onConnWithBreath(View view) {
        TCPClient.with(this)
                .server(ipAddress, 8888)
                .breath("heart...".getBytes(), 10 * 1000)
                .connect(new ITcpNetCallBack() {
                    @Override
                    public void onSuccess() {
                        Log.e(TAG, "onSuccess: withoutBreath");
                    }

                    @Override
                    public void onFail(String failMess) {
                        Log.e(TAG, "onFail: withoutBreath=" + failMess);
                    }
                });
    }

    public void onConnWithBreathClose(View view) {
        TCPClient.closeTcp(ipAddress, 8888);
    }

    public void onSendDataWithBreath(View view) {
        TCPClient.with(this)
                .server(ipAddress, 8888)
                .sendData(etSendData.getText().toString().getBytes(), 8000, new ITcpNetCallBack() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFail(String failMess) {

                    }
                });
    }

    public void onSendDataWithoutBreath(View view) {
        TCPClient.with(this)
                .server(ipAddress, 8888)
                .sendData(etSendData.getText().toString().getBytes(), 8000, new ITcpNetCallBack() {
                    @Override
                    public void onSuccess() {
                        Log.e(TAG, "onSuccess: sendData without breath ok");
                    }

                    @Override
                    public void onFail(String failMess) {
                        Log.e(TAG, "onFail: senddata without breath--" + failMess);
                    }

                    @Override
                    protected void onTimeout() {
                        super.onTimeout();
                        Log.e(TAG, "onTimeout: 请求超时了");
                    }
                });
    }

    class RecTCPBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            MyRecParse myRecParse = new MyRecParse();
            List<RecData> parse = myRecParse.parse();
            if (parse != null) {
                for (RecData data : parse) {
                    byte[] rec = data.getData();
                    Log.e(TAG, "onReceive: " + new String(rec));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(recTCPBroadcast);
        Log.e(TAG, "onDestroy: 执行clear操作");
    }
}