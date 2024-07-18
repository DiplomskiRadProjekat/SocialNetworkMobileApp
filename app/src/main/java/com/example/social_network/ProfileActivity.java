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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.social_network.dtos.FriendRequestDTO;
import com.example.social_network.dtos.FriendRequestStatus;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.fragments.PostsFragment;
import com.example.social_network.services.ServiceUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView buttonRemoveFriend;

    private TextView textViewUsername, textViewName, textViewPosts, textViewFriends;

    private Button buttonAddFriend;

    private LinearLayout linearLayoutResponse, linearLayoutFriends;

    private String token;
    private Long userId;
    private Long myId;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_profile);

        buttonRemoveFriend = findViewById(R.id.trash_icon);
        buttonAddFriend = findViewById(R.id.send_friend_request_button);

        textViewUsername = findViewById(R.id.username);
        textViewName = findViewById(R.id.name);
        textViewFriends = findViewById(R.id.friends);
        textViewPosts = findViewById(R.id.posts);

        linearLayoutResponse = findViewById(R.id.waiting_for_response);
        linearLayoutFriends = findViewById(R.id.friends_layout);

        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("pref_token", "");
        myId = sharedPreferences.getLong("pref_id", 0);

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getLongExtra("userId", 0L);
        }

        if (savedInstanceState == null) {
            PostsFragment fragment = new PostsFragment(false, myId, userId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        fetchFriendRequestStatus();

        setupBottomNavigationListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadUser();

        buttonAddFriend.setOnClickListener(view -> {
            Call<FriendRequestDTO> callAddFriend = ServiceUtils.friendRequestService(token).create(myId, userId);
            callAddFriend.enqueue(new Callback<FriendRequestDTO>() {
                @Override
                public void onResponse(@NonNull Call<FriendRequestDTO> call, @NonNull Response<FriendRequestDTO> response) {
                    if (response.isSuccessful()) {
                        Log.i("Success", response.message());
                        fetchFriendRequestStatus();
                        loadUser();
                    } else {
                        onFailure(call, new Throwable("API call failed with status code: " + response.code()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<FriendRequestDTO> call, Throwable t) {
                    Log.d("Fail", Objects.requireNonNull(t.getMessage()));
                }
            });
        });

        buttonRemoveFriend.setOnClickListener(view -> {
            Call<Void> callRemoveFriend = ServiceUtils.friendRequestService(token).delete(myId, userId);
            callRemoveFriend.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.i("Success", response.message());
                        fetchFriendRequestStatus();
                        loadUser();
                    } else {
                        onFailure(call, new Throwable("API call failed with status code: " + response.code()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, Throwable t) {
                    Log.d("Fail", Objects.requireNonNull(t.getMessage()));
                }
            });
        });

        textViewFriends.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FriendsActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("username", textViewUsername.getText().toString());
            startActivity(intent);
        });
    }

    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_friend_requests) {
                Intent intent = new Intent(ProfileActivity.this, FriendRequestsActivity.class);
                intent.putExtra("userId", myId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(ProfileActivity.this, InboxActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            } else if (itemId == R.id.nav_settings) {
                showSettingsMenu();
                return true;
            }
            return false;
        });
    }

    private void setVisibility(FriendRequestDTO friendRequestDTO) {
        if (!userId.equals(myId)) {
            buttonAddFriend.setVisibility(View.VISIBLE);
            if (friendRequestDTO != null) {
                if (friendRequestDTO.getStatus().equals(FriendRequestStatus.PENDING)) {
                    linearLayoutResponse.setVisibility(View.VISIBLE);
                    buttonAddFriend.setVisibility(View.GONE);
                } else if (friendRequestDTO.getStatus().equals(FriendRequestStatus.ACCEPTED)) {
                    linearLayoutFriends.setVisibility(View.VISIBLE);
                    buttonAddFriend.setVisibility(View.GONE);
                }
            } else {
                linearLayoutResponse.setVisibility(View.GONE);
                linearLayoutFriends.setVisibility(View.GONE);
                buttonAddFriend.setVisibility(View.VISIBLE);
            }
        } else {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }
    }

    private void fetchFriendRequestStatus() {
        Call<FriendRequestDTO> call = ServiceUtils.friendRequestService(token).get(myId, userId);
        call.enqueue(new Callback<FriendRequestDTO>() {
            @Override
            public void onResponse(@NonNull Call<FriendRequestDTO> call, @NonNull Response<FriendRequestDTO> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    FriendRequestDTO friendRequestDTO = response.body();
                    setVisibility(friendRequestDTO);
                } else {
                    if (response.code() == 404) {
                        Log.i("Not Found", "Friend request not found");
                        setVisibility(null);
                    } else {
                        onFailure(call, new Throwable("API call failed with status code: " + response.code()));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<FriendRequestDTO> call, Throwable t) {
                Log.d("Fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void loadUser() {
        Call<UserDTO> call = ServiceUtils.userService(token).get(userId);
        call.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(@NonNull Call<UserDTO> call, @NonNull Response<UserDTO> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    UserDTO userDTO = response.body();
                    if (userDTO != null) {
                        textViewUsername.setText(String.format("@%s", userDTO.getUsername()));
                        textViewName.setText(String.format("%s %s", userDTO.getFirstName(), userDTO.getLastName()));
                        textViewFriends.setText(String.valueOf(userDTO.getFriendsCount()));
                        textViewPosts.setText(String.valueOf(userDTO.getPostsCount()));
                    }
                } else {
                    onFailure(call, new Throwable("API call failed with status code: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDTO> call, Throwable t) {
                Log.d("Fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void showSettingsMenu() {
        View view = findViewById(R.id.nav_settings);
        Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.getMenuInflater().inflate(R.menu.settings_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_profile) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("userId", myId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_delete_profile) {
                showAreYouSureDialog();
                deletePreferences();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                return true;
            } else if (itemId == R.id.action_logout) {
                deletePreferences();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
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
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    Toast.makeText(ProfileActivity.this, "Successfully deleted profile!", Toast.LENGTH_SHORT).show();
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