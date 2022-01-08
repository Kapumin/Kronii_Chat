package com.abjt.chatapplication.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abjt.chatapplication.databinding.ItemReceivedMessageBinding;
import com.abjt.chatapplication.databinding.ItemSentMessageBinding;
import com.abjt.chatapplication.models.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private final List<ChatMessage> chatMessageList;
    private final String senderId;
    public static final int VIEW_TYPE_SEND = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatMessageAdapter(List<ChatMessage> chatMessageList, String senderId) {
        this.chatMessageList = chatMessageList;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SEND) {
            return new SentMessageViewHolder(
                    ItemSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SEND) {
            ((SentMessageViewHolder) holder).setData(chatMessageList.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessageList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessageList.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SEND;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemSentMessageBinding itemSentMessageBinding;

        SentMessageViewHolder(ItemSentMessageBinding itemSentMessageBinding) {
            super(itemSentMessageBinding.getRoot());
            this.itemSentMessageBinding = itemSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            itemSentMessageBinding.sentMessage.setText(chatMessage.message);
            itemSentMessageBinding.sentTimeStamp.setText(chatMessage.dateTime);
        }
    }


    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemReceivedMessageBinding itemReceivedMessageBinding;

        ReceivedMessageViewHolder(ItemReceivedMessageBinding itemReceivedMessageBinding) {
            super(itemReceivedMessageBinding.getRoot());
            this.itemReceivedMessageBinding = itemReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            itemReceivedMessageBinding.receivedMessage.setText(chatMessage.message);
            itemReceivedMessageBinding.receivedMessageTimeStamp.setText(chatMessage.dateTime);
        }

    }
}
