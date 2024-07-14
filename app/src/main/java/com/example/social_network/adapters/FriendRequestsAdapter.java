package com.example.social_network.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.ProfileActivity;
import com.example.social_network.R;
import com.example.social_network.dtos.FriendRequestDTO;
import com.example.social_network.dtos.FriendRequestStatus;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.services.ServiceUtils;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder> {

    private final List<FriendRequestDTO> friendRequests;

    private final Context context;

    private final String token;

    public FriendRequestsAdapter(List<FriendRequestDTO> friendRequests, Context context, String token) {
        this.friendRequests = friendRequests;
        this.context = context;
        this.token = token;
    }

    @NonNull
    @Override
    public FriendRequestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new FriendRequestsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendRequestsAdapter.ViewHolder holder, int position) {
        FriendRequestDTO item = friendRequests.get(position);

        loadUser(item.getFromUserId(), holder.textViewUsername);

        holder.buttonAccept.setOnClickListener(view -> respondToFriendRequest(item.getFromUserId(), item.getToUserId(), FriendRequestStatus.ACCEPTED, position));

        holder.buttonDecline.setOnClickListener(view -> showAreYouSureDialog(item.getFromUserId(), item.getToUserId(), position));

        holder.linearLayoutProfile.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userId", item.getFromUserId());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewProfileImage;

        TextView textViewUsername;

        ImageButton buttonAccept, buttonDecline;

        LinearLayout linearLayoutProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewProfileImage = itemView.findViewById(R.id.profile_image);
            textViewUsername = itemView.findViewById(R.id.username);
            buttonAccept = itemView.findViewById(R.id.acceptButton);
            buttonDecline = itemView.findViewById(R.id.declineButton);
            linearLayoutProfile = itemView.findViewById(R.id.profile);
        }
    }

    private void showAreYouSureDialog(Long fromUserId, Long toUserId, int position) {
        final Dialog dialog = new Dialog(this.context);
        dialog.setContentView(R.layout.popup_are_you_sure);

        Button buttonYes = dialog.findViewById(R.id.yesButton);
        Button buttonNo = dialog.findViewById(R.id.noButton);

        buttonNo.setOnClickListener(v -> dialog.dismiss());

        buttonYes.setOnClickListener(v -> {
            respondToFriendRequest(fromUserId, toUserId, FriendRequestStatus.REJECTED, position);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void respondToFriendRequest(Long fromUserId, Long toUserId, FriendRequestStatus status, int position) {
        Call<FriendRequestDTO> call = ServiceUtils.friendRequestService(token).respondToPendingRequest(fromUserId, toUserId, status);

        call.enqueue(new Callback<FriendRequestDTO>() {
            @Override
            public void onResponse(@NonNull Call<FriendRequestDTO> call, @NonNull Response<FriendRequestDTO> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    if (status.equals(FriendRequestStatus.REJECTED)) {
                        Toast.makeText(context, "Successfully rejected friend request!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Successfully accepted friend request!", Toast.LENGTH_SHORT).show();
                    }
                    friendRequests.remove(position);
                    notifyItemRemoved(position);
                } else {
                    onFailure(call, new Throwable("API call failed with status code: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<FriendRequestDTO> call, Throwable t) {
                Log.d("Fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void loadUser(Long userId, TextView textViewUsername) {
        Call<UserDTO> call = ServiceUtils.userService(token).get(userId);
        call.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(@NonNull Call<UserDTO> call, @NonNull Response<UserDTO> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    UserDTO userDTO = response.body();
                    if (userDTO != null) {
                        textViewUsername.setText(String.format("@%s", userDTO.getUsername()));
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
