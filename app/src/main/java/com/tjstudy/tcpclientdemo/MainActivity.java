package com.tjstudy.tcpclientdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tjstudy.tcpclientdemo.adapter.ChatAdapter;
import com.tjstudy.tcpclientdemo.bean.ChatMess;
import com.tjstudy.tcplib.RequestCallback;
import com.tjstudy.tcplib.ResponseCallback;
import com.tjstudy.tcplib.TCPClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String ipAddress;
    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private EditText etInput;
    private Button btnSend;
    private TCPClient tcpClient;
    private ArrayList<ChatMess> chatMessList;
    private LinearLayoutManager chatLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipAddress = "192.168.5.69";
        initNet();
        initView();
    }

    private void initNet() {
        tcpClient = TCPClient.build()
                .server(ipAddress, 8888)
                .breath("heart".getBytes(), 6 * 1000)
                .connTimeout(10 * 1000);
    }

    private void initView() {
        rvChat = (RecyclerView) findViewById(R.id.recycler_chat);
        chatLayoutManager = new LinearLayoutManager(this);
        rvChat.setLayoutManager(chatLayoutManager);
        chatMessList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessList);
        rvChat.setAdapter(chatAdapter);

        etInput = (EditText) findViewById(R.id.et_input);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String sendMess = etInput.getText().toString();
                ChatMess chatMess = new ChatMess();
                chatMess.setType(2);
                chatMess.setMesg(sendMess);
                chatMessList.add(chatMess);
                chatAdapter.notifyDataSetChanged();
                chatLayoutManager.scrollToPosition(chatMessList.size() - 1);

                tcpClient.request(sendMess.getBytes(), 8000, new RequestCallback() {
                    @Override
                    public void onTimeout() {
                        Log.e(TAG, "onTimeout:请求超时，稍后重试 ,关闭连接 ");
                        TCPClient.closeTcp(ipAddress, 8888);
                    }

                    @Override
                    public void onFail(Throwable throwable) {
                        handlerError(throwable);
                    }
                }, responseCallback);
            }
        });
    }

    private void handlerError(Throwable throwable) {
        Log.e(TAG, "handlerError: 网络访问失败:" + throwable.getMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TCPClient.closeTcp(ipAddress, 8888);
    }

    private ResponseCallback responseCallback = new ResponseCallback() {
        @Override
        public void onRec() {
            MyRecParse myRecParse = new MyRecParse();
            List<RecData> dataList = myRecParse.parse();
            if (dataList.size() > 0) {
                for (RecData recData :
                        dataList) {
                    ChatMess chatMess = new ChatMess();
                    chatMess.setType(1);
                    chatMess.setMesg(new String(recData.getData()));
                    chatMessList.add(chatMess);
                    chatAdapter.notifyDataSetChanged();
                    chatLayoutManager.scrollToPosition(chatMessList.size() - 1);
                }
            }
        }

        @Override
        public void onFail(Throwable throwable) {
            handlerError(throwable);
        }
    };

    /**
     * 测试场景：不请求数据时，进行数据的接收 心跳时间间隔改短 6s
     *
     * @param view
     */
    public void onRecHeartTest(View view) {
        tcpClient.onResponse(responseCallback);
    }
}