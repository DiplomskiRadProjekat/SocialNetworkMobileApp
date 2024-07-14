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
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.services.ServiceUtils;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private final List<UserDTO> friends;

    private final Context context;

    private final String token;

    private final Long myId;

    private final boolean myProfile;

    public FriendsAdapter(List<UserDTO> friends, Context context, String token, Long myId, boolean myProfile) {
        this.friends = friends;
        this.context = context;
        this.token = token;
        this.myId = myId;
        this.myProfile = myProfile;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserDTO item = friends.get(position);

        holder.textViewUsername.setText(String.format("@%s", item.getUsername()));

        holder.buttonDelete.setOnClickListener(view -> showAreYouSureDialog(item.getId(), position));

        holder.linearLayoutProfile.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userId", item.getId());
            context.startActivity(intent);
        });

        if (myProfile) {
            holder.buttonDelete.setVisibility(View.VISIBLE);
        } else {
            holder.buttonDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewProfileImage;

        TextView textViewUsername;

        ImageButton buttonDelete;

        LinearLayout linearLayoutProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewProfileImage = itemView.findViewById(R.id.profile_image);
            textViewUsername = itemView.findViewById(R.id.username);
            buttonDelete = itemView.findViewById(R.id.delete_button);
            linearLayoutProfile = itemView.findViewById(R.id.profile);
        }
    }

    private void showAreYouSureDialog(Long friendsId, int position) {
        final Dialog dialog = new Dialog(this.context);
        dialog.setContentView(R.layout.popup_are_you_sure);

        Button buttonYes = dialog.findViewById(R.id.yesButton);
        Button buttonNo = dialog.findViewById(R.id.noButton);

        buttonNo.setOnClickListener(v -> dialog.dismiss());

        buttonYes.setOnClickListener(v -> {
            deleteFriend(friendsId, position);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteFriend(Long friendsId, int position) {
        Call<Void> call = ServiceUtils.friendRequestService(token).delete(myId, friendsId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    Toast.makeText(context, "Successfully deleted friend!", Toast.LENGTH_SHORT).show();
                    friends.remove(position);
                    notifyItemRemoved(position);
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

}
