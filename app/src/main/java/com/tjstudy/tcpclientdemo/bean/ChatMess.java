package com.tjstudy.tcpclientdemo.bean;

/**
 * 聊天 消息实体
 */

public class ChatMess {
    private int type;
    private String mesg;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMesg() {
        return mesg;
    }

    public void setMesg(String mesg) {
        this.mesg = mesg;
    }
}
