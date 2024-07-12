package com.example.social_network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.social_network.dtos.FriendRequestDTO;
import com.example.social_network.dtos.FriendRequestStatus;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.services.ServiceUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage, buttonRemoveFriend;

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

        profileImage = findViewById(R.id.profile_image);

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
    }

    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                //TODO: dodaj home
                // startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_friend_requests) {
                //TODO: dodaj friend requests
                // startActivity(new Intent(ProfileActivity.this, FriendRequestsActivity.class));
                return true;
            } else if (itemId == R.id.nav_chat) {
                //TODO: dodaj chat
                // startActivity(new Intent(ProfileActivity.this, ChatActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
//                Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
//                intent.putExtra("userId", myId);
//                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("userId", myId);
                startActivity(intent);
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

}