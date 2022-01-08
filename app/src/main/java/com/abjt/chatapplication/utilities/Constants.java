package com.abjt.chatapplication.utilities;

import com.abjt.chatapplication.R;

import java.util.HashMap;

public class Constants {

    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "kroniiChatPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userID";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIME_STAMP = "timeStamp";
    public static final String KEY_COLLECTION_SPECIFIC_CHAT = "specificChat";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_STATUS = "status";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";


    public static final String REMOTE_MSG_RECIPIENT = "to";
    public static final String notification = "notification";
    public static final String title = "title";
    public static final String body = "body";
    public static final String click_action = "click_action";
    public static final String click_action_value = "ChatActivity";
    public static final String icon="icon";
    public static final String notification_color="color";
    public static final String check_notification_intent="yes";
    public static final String android_channel_id="android_channel_id";
    public static final String android_channel_id_value="Chat_Message";




    //Using Retrofit to send Push Notification
    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders(){
        if (remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=your_key_here"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}
