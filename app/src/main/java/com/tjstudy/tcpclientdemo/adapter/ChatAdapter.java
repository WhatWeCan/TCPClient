package com.tjstudy.tcpclientdemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tjstudy.tcpclientdemo.R;
import com.tjstudy.tcpclientdemo.bean.ChatMess;

import java.util.List;

/**
 * Chat adapter
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
    List<ChatMess> chatMessList;
    Context mContext;

    public ChatAdapter(Context mContext, List<ChatMess> chatMessList) {
        this.chatMessList = chatMessList;
        this.mContext = mContext;
    }

    @Override
    public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatHolder(LayoutInflater.from(mContext).inflate(R.layout.item_rv_chat, parent, false));
    }

    @Override
    public void onBindViewHolder(ChatHolder holder, int position) {
        ChatMess chatMess = chatMessList.get(position);
        if (chatMess.getType() == 1) {
            holder.tvSend.setVisibility(View.GONE);
            holder.tvRec.setVisibility(View.VISIBLE);
            holder.tvRec.setText(chatMess.getMesg());
        } else {
            holder.tvRec.setVisibility(View.GONE);
            holder.tvSend.setVisibility(View.VISIBLE);
            holder.tvSend.setText(chatMess.getMesg());
        }
    }

    @Override
    public int getItemCount() {
        return chatMessList.size();
    }

    public class ChatHolder extends RecyclerView.ViewHolder {

        TextView tvSend;
        TextView tvRec;

        public ChatHolder(View itemView) {
            super(itemView);
            tvSend = (TextView) itemView.findViewById(R.id.tv_send);
            tvRec = (TextView) itemView.findViewById(R.id.tv_rec);
        }
    }
}
