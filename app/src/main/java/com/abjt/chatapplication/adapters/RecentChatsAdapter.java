package com.abjt.chatapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abjt.chatapplication.databinding.ItemRecentChatBinding;
import com.abjt.chatapplication.listeners.SpecificChatListener;
import com.abjt.chatapplication.models.ChatMessage;
import com.abjt.chatapplication.models.User;

import java.util.List;

public class RecentChatsAdapter extends RecyclerView.Adapter<RecentChatsAdapter.RecentChatViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private final SpecificChatListener specificChatListener;

    public RecentChatsAdapter(List<ChatMessage> chatMessageList, SpecificChatListener specificChatListener) {
        this.chatMessageList = chatMessageList;
        this.specificChatListener = specificChatListener;
    }

    @NonNull
    @Override
    public RecentChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentChatViewHolder(
                ItemRecentChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecentChatViewHolder holder, int position) {
        holder.setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    public
    class RecentChatViewHolder extends RecyclerView.ViewHolder {
        private ItemRecentChatBinding itemRecentChatBinding;

        RecentChatViewHolder(ItemRecentChatBinding itemRecentChatBinding) {
            super(itemRecentChatBinding.getRoot());
            this.itemRecentChatBinding = itemRecentChatBinding;
        }

        void setData(ChatMessage chatMessage) {
            itemRecentChatBinding.userProfileImage.setImageBitmap(getChatImage(chatMessage.chatImage));
            itemRecentChatBinding.username.setText(chatMessage.chatName);
            itemRecentChatBinding.recentMessage.setText(chatMessage.message);
            itemRecentChatBinding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.chatId;
                user.name = chatMessage.chatName;
                user.image = chatMessage.chatImage;
                specificChatListener.onSpecificChatClicked(user);
            });
        }
    }

    private Bitmap getChatImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
