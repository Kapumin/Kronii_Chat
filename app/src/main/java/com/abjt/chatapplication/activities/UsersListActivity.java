package com.abjt.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.abjt.chatapplication.R;
import com.abjt.chatapplication.adapters.UserAdapter;
import com.abjt.chatapplication.databinding.ActivityUsersListBinding;
import com.abjt.chatapplication.listeners.UserListener;
import com.abjt.chatapplication.models.User;
import com.abjt.chatapplication.utilities.Constants;
import com.abjt.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersListActivity extends StatusActivity implements UserListener {

    private ActivityUsersListBinding activityUsersListBinding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityUsersListBinding = ActivityUsersListBinding.inflate(getLayoutInflater());
        setContentView(activityUsersListBinding.getRoot());
        activityUsersListBinding.loading.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.primary), android.graphics.PorterDuff.Mode.MULTIPLY);
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }


    private void setListeners(){
        activityUsersListBinding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            userList.add(user);
                        }
                        if (userList.size() > 0) {
                            UserAdapter userAdapter = new UserAdapter(userList,this);
                            activityUsersListBinding.usersRecyclerView.setAdapter(userAdapter);
                            activityUsersListBinding.usersRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else showErrorMessage();
                });
    }

    private void showErrorMessage() {
        activityUsersListBinding.textErrorMessage.setText(String.format("%s", "Friends List Empty"));
        activityUsersListBinding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            activityUsersListBinding.loading.setVisibility(View.VISIBLE);
        } else {
            activityUsersListBinding.loading.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}