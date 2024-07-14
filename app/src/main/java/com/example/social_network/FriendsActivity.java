package com.example.social_network;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.social_network.fragments.FriendsFragment;
import com.example.social_network.services.ServiceUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsActivity extends AppCompatActivity {

    private String token;

    private Long userId;

    private Long myId;

    private BottomNavigationView bottomNavigationView;

    private TextView textViewTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_friends);

        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("pref_token", "");
        myId = sharedPreferences.getLong("pref_id", 0);

        textViewTitle = findViewById(R.id.title);

        Intent intent = getIntent();
        if (intent != null) {
            textViewTitle.setText(String.format("%s's friends list: ", intent.getStringExtra("username")));
            userId = intent.getLongExtra("userId", 0L);
        }

        boolean myProfile = myId.equals(userId);

        if (savedInstanceState == null) {
            FriendsFragment fragment = new FriendsFragment(userId, myProfile);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigationListener();
    }

    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                //TODO: dodaj home
                // startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_friend_requests) {
                //TODO: dodaj friend requests
                // startActivity(new Intent(EditProfileActivity.this, FriendRequestsActivity.class));
                return true;
            } else if (itemId == R.id.nav_chat) {
                //TODO: dodaj chat
                // startActivity(new Intent(EditProfileActivity.this, ChatActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(FriendsActivity.this, ProfileActivity.class);
                intent.putExtra("userId", myId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                showSettingsMenu();
                return true;
            }
            return false;
        });

        bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
    }

    private void showSettingsMenu() {
        View view = findViewById(R.id.nav_settings);
        Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.getMenuInflater().inflate(R.menu.settings_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_profile) {
                Intent intent = new Intent(FriendsActivity.this, EditProfileActivity.class);
                intent.putExtra("userId", myId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_delete_profile) {
                showAreYouSureDialog();
                return true;
            } else if (itemId == R.id.action_logout) {
                deletePreferences();
                startActivity(new Intent(FriendsActivity.this, LoginActivity.class));
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showAreYouSureDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_are_you_sure);

        Button buttonYes = dialog.findViewById(R.id.yesButton);
        Button buttonNo = dialog.findViewById(R.id.noButton);

        buttonNo.setOnClickListener(v -> dialog.dismiss());

        buttonYes.setOnClickListener(v -> {
            deleteProfile();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteProfile() {
        Call<Void> call = ServiceUtils.userService(token).delete(myId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    deletePreferences();
                    startActivity(new Intent(FriendsActivity.this, LoginActivity.class));
                    Toast.makeText(FriendsActivity.this, "Successfully deleted profile!", Toast.LENGTH_SHORT).show();
                } else {
                    onFailure(call, new Throwable("API call failed with status code: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, Throwable t) {
                Log.d("Fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void deletePreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.clear().apply();
    }
}