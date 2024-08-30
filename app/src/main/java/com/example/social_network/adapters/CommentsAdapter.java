package com.example.social_network.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.R;
import com.example.social_network.dtos.CommentDTO;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.services.ServiceUtils;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private final List<CommentDTO> comments;

    private final Context context;

    private final String token;

    private final Long myId;

    public CommentsAdapter(List<CommentDTO> comments, Context context, String token, Long myId) {
        this.comments = comments;
        this.context = context;
        this.token = token;
        this.myId = myId;
    }

    @NonNull
    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentsAdapter.ViewHolder holder, int position) {
        CommentDTO item = comments.get(position);

        loadProfilePicture(item.getUserId(), holder.imageViewProfilePicture);

        loadUsername(item.getUserId(), holder.textViewUsername);

        LocalDateTime dateTime = LocalDateTime.parse(item.getCommentedAt(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));
        holder.textViewTimestamp.setText(dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        holder.textViewComment.setText(item.getComment());

        if (myId.equals(item.getUserId())) {
            holder.imageViewDeleteComment.setVisibility(View.VISIBLE);
        }

        holder.imageViewDeleteComment.setOnClickListener(view -> showAreYouSureDialog(item.getId(), position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUsername, textViewTimestamp, textViewComment;

        ImageView imageViewDeleteComment, imageViewProfilePicture;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.username);
            textViewTimestamp = itemView.findViewById(R.id.timestamp);
            textViewComment = itemView.findViewById(R.id.comment);
            imageViewDeleteComment = itemView.findViewById(R.id.delete_comment);
            imageViewProfilePicture = itemView.findViewById(R.id.profile_image);
        }
    }

    private void showAreYouSureDialog(Long commentId, int position) {
        final Dialog dialog = new Dialog(this.context);
        dialog.setContentView(R.layout.popup_are_you_sure);

        Button buttonYes = dialog.findViewById(R.id.yesButton);
        Button buttonNo = dialog.findViewById(R.id.noButton);

        buttonNo.setOnClickListener(v -> dialog.dismiss());

        buttonYes.setOnClickListener(v -> {
            deleteComment(commentId, position);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteComment(Long commentId, int position) {
        Call<Void> call = ServiceUtils.commentService(token).delete(commentId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    Toast.makeText(context, "Successfully deleted comment!", Toast.LENGTH_SHORT).show();
                    comments.remove(position);
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

    private void loadUsername(Long userId, TextView textViewUsername) {
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
