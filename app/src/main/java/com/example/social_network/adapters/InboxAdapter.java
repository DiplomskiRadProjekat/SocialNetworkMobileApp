package com.example.social_network.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.ChatActivity;
import com.example.social_network.R;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.services.ServiceUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private final List<UserDTO> friends;

    private final Context context;

    private final String token;

    public InboxAdapter(List<UserDTO> friends, Context context, String token) {
        this.friends = friends;
        this.context = context;
        this.token = token;
    }

    @NonNull
    @Override
    public InboxAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_chat, parent, false);
        return new InboxAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InboxAdapter.ViewHolder holder, int position) {
        UserDTO item = friends.get(position);

        loadProfilePicture(item.getId(), holder.imageViewProfilePicture);

        holder.textViewUsername.setText(String.format("@%s", item.getUsername()));

        holder.linearLayoutProfile.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("friendId", item.getUid());
            intent.putExtra("friendUsername", item.getUsername());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUsername;

        LinearLayout linearLayoutProfile;

        ImageView imageViewProfilePicture;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.username);
            linearLayoutProfile = itemView.findViewById(R.id.profile);
            imageViewProfilePicture = itemView.findViewById(R.id.profile_image);
        }
    }

    private void loadProfilePicture(Long id, ImageView imageViewProfilePicture) {
        Call<ResponseBody> call = ServiceUtils.userService(token).downloadProfilePicture(id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    assert response.body() != null;
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        ImageViewCompat.setImageTintList(imageViewProfilePicture, null);
                        imageViewProfilePicture.setImageBitmap(bitmap);
                    } else {
                        Log.e("LoadImage", "Failed to decode bitmap from stream");
                    }

                } else {
                    onFailure(call, new Throwable("API call failed with status code: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
                Log.d("Fail", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

}
