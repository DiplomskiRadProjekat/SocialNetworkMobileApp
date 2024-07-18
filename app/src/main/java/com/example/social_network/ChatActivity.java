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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.adapters.MessageAdapter;
import com.example.social_network.dtos.MessageModel;
import com.example.social_network.services.ServiceUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private String receiverId, receiverUsername, senderId, senderUsername, senderRoom, receiverRoom;

    private DatabaseReference dbReferenceReceiver, dbReferenceSender, userReference;

    private EditText editTextMessage;

    private ImageButton imageButtonSendMessage;

    private TextView textViewTitle;

    private RecyclerView recyclerView;

    private MessageAdapter messageAdapter;

    private String token;

    private Long myId;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_chat);

        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("pref_token", "");
        myId = sharedPreferences.getLong("pref_id", 0);

        userReference = FirebaseDatabase.getInstance("https://socialnetwork-220d1-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users");
        receiverId = getIntent().getStringExtra("friendId");
        receiverUsername = getIntent().getStringExtra("friendUsername");

        if (receiverId != null) {
            senderRoom = FirebaseAuth.getInstance().getUid() + receiverId;
            receiverRoom = receiverId + FirebaseAuth.getInstance().getUid();
        }

        editTextMessage = findViewById(R.id.message);
        imageButtonSendMessage = findViewById(R.id.sendButton);
        recyclerView = findViewById(R.id.recyclerView);
        textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(String.format("@%s", receiverUsername));

        messageAdapter = new MessageAdapter(this);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbReferenceSender = FirebaseDatabase.getInstance("https://socialnetwork-220d1-default-rtdb.europe-west1.firebasedatabase.app").getReference("Chats").child(senderRoom);
        dbReferenceReceiver = FirebaseDatabase.getInstance("https://socialnetwork-220d1-default-rtdb.europe-west1.firebasedatabase.app").getReference("Chats").child(receiverRoom);

        dbReferenceSender.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MessageModel> messages = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    messages.add(messageModel);
                    Log.d("ChatActivity", "Loaded message: " + messageModel.getMessage());
                }
                messageAdapter.clear();
                for(MessageModel messageModel : messages) {
                    messageAdapter.add(messageModel);
                }
                messageAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imageButtonSendMessage.setOnClickListener(view -> {
            String message = editTextMessage.getText().toString();

            boolean hasError = false;
            if (message.isEmpty()) {
                setError(editTextMessage);
                hasError = true;
            } else {
                resetError(editTextMessage);
            }

            if(!hasError) {
                sendMessage(message);
            }

        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_chat);
        setupBottomNavigationListener();
    }

    private void sendMessage(String message){
        String messageId = UUID.randomUUID().toString();
        MessageModel messageModel = new MessageModel(messageId, FirebaseAuth.getInstance().getUid(), message, System.currentTimeMillis());
        messageAdapter.add(messageModel);

        dbReferenceSender.child(messageId).setValue(messageModel)
                .addOnSuccessListener(unused -> Log.d("ChatActivity", "Message sent successfully"))
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show());

        dbReferenceReceiver.child(messageId).setValue(messageModel);
        recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
        editTextMessage.setText("");
    }

    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ChatActivity.this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_friend_requests) {
                Intent intent = new Intent(ChatActivity.this, FriendRequestsActivity.class);
                intent.putExtra("userId", myId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(ChatActivity.this, InboxActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("userId", myId);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                showSettingsMenu();
                return true;
            }
            return false;
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
                return true;
            } else if (itemId == R.id.action_delete_profile) {
                showAreYouSureDialog();
                return true;
            } else if (itemId == R.id.action_logout) {
                deletePreferences();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChatActivity.this, LoginActivity.class));
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
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(ChatActivity.this, LoginActivity.class));
                    Toast.makeText(ChatActivity.this, "Successfully deleted profile!", Toast.LENGTH_SHORT).show();
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

    private void setError(EditText editText) {
        editText.setBackgroundResource(R.drawable.edit_text_error);
    }

    private void resetError(EditText editText) {
        editText.setBackgroundResource(R.drawable.edit_text);
    }
}