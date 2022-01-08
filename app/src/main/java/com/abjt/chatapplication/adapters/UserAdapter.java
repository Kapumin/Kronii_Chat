package com.abjt.chatapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abjt.chatapplication.databinding.ItemUserBinding;
import com.abjt.chatapplication.listeners.UserListener;
import com.abjt.chatapplication.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private final UserListener userListener;

    public UserAdapter(List<User> userList, UserListener userListener) {
        this.userList = userList;
        this.userListener =  userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding itemUserBinding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(itemUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemUserBinding itemUserBinding;

        UserViewHolder(ItemUserBinding itemUserBinding) {
            super(itemUserBinding.getRoot());
            this.itemUserBinding = itemUserBinding;
        }

        void setUserData(User user) {
            itemUserBinding.username.setText(user.name);
            itemUserBinding.userEmail.setText(user.email);
            itemUserBinding.userProfileImage.setImageBitmap(getUserProfileImage(user.image));
            itemUserBinding.getRoot().setOnClickListener(v-> userListener.onUserClicked(user));
        }
    }

    private Bitmap getUserProfileImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


}
