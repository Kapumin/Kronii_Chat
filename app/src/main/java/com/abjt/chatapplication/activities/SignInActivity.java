package com.abjt.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.abjt.chatapplication.R;
import com.abjt.chatapplication.databinding.ActivitySignInBinding;
import com.abjt.chatapplication.utilities.Constants;
import com.abjt.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding activitySignInBinding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        activitySignInBinding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(activitySignInBinding.getRoot());
        activitySignInBinding.loading.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.primary), android.graphics.PorterDuff.Mode.MULTIPLY);
        setListeners();
    }

    private void setListeners() {
        activitySignInBinding.textCreateNewAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        activitySignInBinding.signInButton.setOnClickListener(v -> {
            SignIn();
        });
    }

    private void SignIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, activitySignInBinding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, activitySignInBinding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Sorry! Log In Failed");
                    }
                });
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            activitySignInBinding.signInButton.setVisibility(View.INVISIBLE);
            activitySignInBinding.loading.setVisibility(View.VISIBLE);
        } else {
            activitySignInBinding.loading.setVisibility(View.INVISIBLE);
            activitySignInBinding.signInButton.setVisibility(View.VISIBLE);
        }

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails() {
        if (activitySignInBinding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(activitySignInBinding.inputEmail.getText().toString()).matches()) {
            showToast("Enter a  Valid Email Address");
            return false;
        } else if (activitySignInBinding.inputPassword.getText().toString().isEmpty()) {
            showToast("Enter Password");
            return false;
        } else return true;
    }
}

