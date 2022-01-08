package com.abjt.chatapplication.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.abjt.chatapplication.R;
import com.abjt.chatapplication.adapters.ChatMessageAdapter;
import com.abjt.chatapplication.databinding.ActivityChatBinding;
import com.abjt.chatapplication.models.ChatMessage;
import com.abjt.chatapplication.models.User;
import com.abjt.chatapplication.network.ApiClient;
import com.abjt.chatapplication.network.ApiService;
import com.abjt.chatapplication.utilities.Constants;
import com.abjt.chatapplication.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends StatusActivity {

    private ActivityChatBinding activityChatBinding;
    private User recipientUser;
    private List<ChatMessage> chatMessageList;
    private PreferenceManager preferenceManager;
    private ChatMessageAdapter chatMessageAdapter;
    private FirebaseFirestore database;
    private String chatId = null;
    private Boolean isReceiverAvailable = false;
    public static final String TAG = ChatActivity.class.getSimpleName();
    private boolean userSet = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityChatBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(activityChatBinding.getRoot());
        setListeners();
        init();
        getExtrasFromNotification();
        loadRecipientDetails();
        listenMessage();
    }

    private void getExtrasFromNotification() {
        if (getIntent().getStringExtra(Constants.check_notification_intent)!=null){
            if (getIntent().getStringExtra(Constants.check_notification_intent).equals("yes")){
                recipientUser = new User();
                recipientUser.name = getIntent().getStringExtra(Constants.KEY_NAME);
                recipientUser.id = getIntent().getStringExtra(Constants.KEY_USER_ID);
                recipientUser.email = getIntent().getStringExtra(Constants.KEY_EMAIL);
                recipientUser.token = getIntent().getStringExtra(Constants.KEY_FCM_TOKEN);
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_USER_ID,recipientUser.id)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult()!=null){
                                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                                        if ((recipientUser.id).equals(queryDocumentSnapshot.getId())){
                                            recipientUser.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                            Log.d(TAG,"recipient Image : "+recipientUser.image);
                                        }
                                    }

                                }
                            });
                userSet = true;
            }
        }
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatMessageAdapter = new ChatMessageAdapter(chatMessageList, preferenceManager.getString(Constants.KEY_USER_ID));
        activityChatBinding.chatRecyclerView.setAdapter(chatMessageAdapter);
        database = FirebaseFirestore.getInstance();
        activityChatBinding.loading.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.primary), android.graphics.PorterDuff.Mode.MULTIPLY);
    }


    private Bitmap getBitmapFromEncodedImage(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void loadRecipientDetails() {
        if (!userSet){
            recipientUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        }
        activityChatBinding.recipientProfileImage.setImageBitmap(getBitmapFromEncodedImage(recipientUser.image));
        activityChatBinding.recipientName.setText(recipientUser.name);
    }

    private void setListeners() {
        activityChatBinding.imageBack.setOnClickListener(v -> onBackPressed());
        activityChatBinding.send.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, recipientUser.id);
        message.put(Constants.KEY_MESSAGE, activityChatBinding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIME_STAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);

        if (chatId != null) {
            updateRecentChat(activityChatBinding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> chat = new HashMap<>();
            chat.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            chat.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            chat.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            chat.put(Constants.KEY_RECEIVER_ID, recipientUser.id);
            chat.put(Constants.KEY_RECEIVER_NAME, recipientUser.name);
            chat.put(Constants.KEY_RECEIVER_IMAGE, recipientUser.image);
            chat.put(Constants.KEY_LAST_MESSAGE, activityChatBinding.inputMessage.getText().toString());
            chat.put(Constants.KEY_TIME_STAMP, new Date());
            addRecentChat(chat);
        }
        if (!isReceiverAvailable) {
            try {

                JSONObject notification = new JSONObject();
                notification.put(Constants.title, preferenceManager.getString(Constants.KEY_NAME));
                notification.put(Constants.body, activityChatBinding.inputMessage.getText().toString());
                notification.put(Constants.click_action, Constants.click_action_value);
                notification.put(Constants.android_channel_id,Constants.android_channel_id_value);
                notification.put(Constants.icon, String.valueOf(R.drawable.ic_new_message_available));
                notification.put(Constants.notification_color, String.valueOf(R.color.primary));

                JSONObject data = new JSONObject();
                data.put(Constants.check_notification_intent,"yes");
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
//                data.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
                data.put(Constants.KEY_MESSAGE, activityChatBinding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.notification, notification);
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_RECIPIENT, recipientUser.token);

                Log.d(TAG, "notification :" + notification.toString());
                Log.d(TAG, "to :" + recipientUser.token);
                Log.d(TAG, "data :" + data.toString());
                Log.d(TAG, "body :" + body.toString());

                sendingNotification(body.toString());

            } catch (Exception e) {
                showToast(e.getMessage());
            }
        }
        activityChatBinding.inputMessage.setText(null);
    }

    private void listenStatus() {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(recipientUser.id)
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        if (value.getLong(Constants.KEY_STATUS) != null) {
                            int status = Objects.requireNonNull(value.getLong(Constants.KEY_STATUS)).intValue();
                            isReceiverAvailable = status == 1;
                        }
                        recipientUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                    }
                    if (isReceiverAvailable) {
                        activityChatBinding.status.setText(R.string.online);
                        activityChatBinding.status.setTextColor(getResources().getColor(R.color.onlineGreen));

                    } else {
                        activityChatBinding.status.setText(R.string.offline);
                        activityChatBinding.status.setTextColor(getResources().getColor(R.color.white_text));
                    }
                    activityChatBinding.status.setVisibility(View.VISIBLE);
                });

    }

    private void listenMessage() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, recipientUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, recipientUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }


    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessageList.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getRealDate(documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIME_STAMP);
                    chatMessageList.add(chatMessage);
                }
            }
            Collections.sort(chatMessageList, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatMessageAdapter.notifyDataSetChanged();
            } else {
                chatMessageAdapter.notifyItemRangeInserted(chatMessageList.size(), chatMessageList.size());
                activityChatBinding.chatRecyclerView.smoothScrollToPosition(chatMessageList.size() - 1);
            }
            activityChatBinding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        activityChatBinding.loading.setVisibility(View.GONE);
        if (chatId == null) {
            checkForRecentChat();
        }
    };


    private String getRealDate(Date date) {
        return new SimpleDateFormat("MMMM dd , yyyy - hh:mm a", Locale.getDefault()).format(date);
    }


    private void checkForRecentChat() {
        if (chatMessageList.size() != 0) {
            checkForChatRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    recipientUser.id
            );
            checkForChatRemotely(
                    recipientUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForChatRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_SPECIFIC_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(chatCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> chatCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            chatId = documentSnapshot.getId();
        }
    };

    //Adding the recentChat to database
    private void addRecentChat(HashMap<String, Object> chat) {
        database.collection(Constants.KEY_COLLECTION_SPECIFIC_CHAT)
                .add(chat)
                .addOnSuccessListener(documentReference -> chatId = documentReference.getId());
    }

    private void updateRecentChat(String message) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_SPECIFIC_CHAT)
                .document(chatId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIME_STAMP, new Date()
        );
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendingNotification(String messageBody) {

        ApiClient.getRetrofit().create(ApiService.class).sendMessage(Constants.getRemoteMsgHeaders(), messageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            try {
                                if (response.body() != null) {
                                    Log.d(TAG,"Sent");
                                    JSONObject responseJson = new JSONObject(response.body());
                                    JSONArray results = responseJson.getJSONArray("results");
                                    if (responseJson.getInt("failure") == 1) {
                                        JSONObject error = (JSONObject) results.get(0);
                                        showToast(error.getString("error"));
                                        return;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            showToast("Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        showToast(t.getMessage());
                        Log.d(TAG,"FAILED");
                        Log.d(TAG,"FAILED -> "+t.getMessage());

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenStatus();
    }
}