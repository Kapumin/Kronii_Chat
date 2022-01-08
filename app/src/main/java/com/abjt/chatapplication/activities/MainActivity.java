package com.abjt.chatapplication.activities;

import androidx.core.view.GravityCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuInflater;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.abjt.chatapplication.R;
import com.abjt.chatapplication.adapters.RecentChatsAdapter;
import com.abjt.chatapplication.databinding.ActivityMainBinding;
import com.abjt.chatapplication.databinding.NavigationHeaderBinding;
import com.abjt.chatapplication.listeners.SpecificChatListener;
import com.abjt.chatapplication.models.ChatMessage;
import com.abjt.chatapplication.models.User;
import com.abjt.chatapplication.utilities.Constants;
import com.abjt.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends StatusActivity implements SpecificChatListener {

    private ActivityMainBinding activityMainBinding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> chatMessageList;
    private RecentChatsAdapter recentChatsAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        createChannel();
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenChat();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.channel_id);
            CharSequence channelName = "Chat Message";
            String channelDescription = "This notification channel is for new chat message";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }

    private void init() {
        activityMainBinding.loading.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.primary), android.graphics.PorterDuff.Mode.MULTIPLY);
        chatMessageList = new ArrayList<>();
        recentChatsAdapter = new RecentChatsAdapter(chatMessageList, this);
        activityMainBinding.recentChatRecyclerVIew.setAdapter(recentChatsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    @SuppressLint("NonConstantResourceId")
    private void setListeners() {
        activityMainBinding.imageOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v, GravityCompat.END);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.header_options, popupMenu.getMenu());
            popupMenu.show();

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.profileOptions:
                        break;
                    case R.id.logoutOptions:
                        signOut();
                        break;
                }

                return true;
            });
        });
        activityMainBinding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UsersListActivity.class)));
    }

    private void loadUserDetails() {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        View navigationHeader = activityMainBinding.navigationDrawer.getHeaderView(0);
        RoundedImageView profileImage = navigationHeader.findViewById(R.id.profileImageUser);
        TextView username = navigationHeader.findViewById(R.id.user_name);
        TextView email = navigationHeader.findViewById(R.id.user_email);
        profileImage.setImageBitmap(bitmap);
        username.setText(preferenceManager.getString(Constants.KEY_NAME));
        email.setText(preferenceManager.getString(Constants.KEY_EMAIL));

        activityMainBinding.drawerButton.setOnClickListener(v -> activityMainBinding.drawerLayout.openDrawer(GravityCompat.START));

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Token Update Failed"));
    }


    private void listenChat() {
        database.collection(Constants.KEY_COLLECTION_SPECIFIC_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_SPECIFIC_CHAT)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.chatId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.chatName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.chatImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                    } else {
                        chatMessage.chatId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.chatName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.chatImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP);
                    chatMessageList.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < chatMessageList.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (chatMessageList.get(i).senderId.equals(senderId) && chatMessageList.get(i).receiverId.equals(receiverId)) {
                            chatMessageList.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            chatMessageList.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP);
                            break;
                        }
                    }
                }
            }

            Collections.sort(chatMessageList, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            recentChatsAdapter.notifyDataSetChanged();
            activityMainBinding.recentChatRecyclerVIew.smoothScrollToPosition(0);
            activityMainBinding.recentChatRecyclerVIew.setVisibility(View.VISIBLE);
            activityMainBinding.loading.setVisibility(View.GONE);
        }
    };


    private void signOut() {
        showToast("Signing Out..");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Sign Out Failed"));
    }

    @Override
    public void onSpecificChatClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (activityMainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }
}