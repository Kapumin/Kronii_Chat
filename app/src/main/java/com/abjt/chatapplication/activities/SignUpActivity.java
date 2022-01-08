package com.abjt.chatapplication.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.abjt.chatapplication.R;
import com.abjt.chatapplication.databinding.ActivitySignUpBinding;
import com.abjt.chatapplication.utilities.Constants;
import com.abjt.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = SignUpActivity.class.getSimpleName();
    private ActivitySignUpBinding activitySignUpBinding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activitySignUpBinding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(activitySignUpBinding.getRoot());
        activitySignUpBinding.loading.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), android.graphics.PorterDuff.Mode.MULTIPLY);
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        activitySignUpBinding.textSignIn.setOnClickListener(v -> onBackPressed());
        activitySignUpBinding.signUpButton.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });
        activitySignUpBinding.layoutProfileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {

        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, activitySignUpBinding.inputUsername.getText().toString());
        user.put(Constants.KEY_EMAIL, activitySignUpBinding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, activitySignUpBinding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Sign Up Successful");
                    loading(false);
                    Toast.makeText(getApplicationContext(), "Sign Up Successful", Toast.LENGTH_SHORT).show();

                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, activitySignUpBinding.inputUsername.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    preferenceManager.putString(Constants.KEY_EMAIL, activitySignUpBinding.inputEmail.getText().toString());
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    Log.d(TAG, "Sign Up Failed");
                    loading(false);
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getEncodedImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            activitySignUpBinding.imageProfile.setImageBitmap(bitmap);
                            activitySignUpBinding.textAddImage.setVisibility(View.GONE);
                            encodedImage = getEncodedImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast("Add Profile Image");
            return false;
        } else if (activitySignUpBinding.inputUsername.getText().toString().trim().isEmpty()) {
            showToast("Enter Username");
            return false;
        } else if (activitySignUpBinding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(activitySignUpBinding.inputEmail.getText().toString()).matches()) {
            showToast("Enter Valid Email Address");
            return false;
        } else if (activitySignUpBinding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm Your Password");
            return false;
        } else if (!activitySignUpBinding.inputPassword.getText().toString()
                .equals(activitySignUpBinding.inputConfirmPassword.getText().toString())) {
            showToast("Password not Same");
            return false;
        } else return true;
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            activitySignUpBinding.signUpButton.setVisibility(View.INVISIBLE);
            activitySignUpBinding.loading.setVisibility(View.VISIBLE);
        } else {
            activitySignUpBinding.loading.setVisibility(View.INVISIBLE);
            activitySignUpBinding.signUpButton.setVisibility(View.VISIBLE);
        }
    }

}