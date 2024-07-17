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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.R;
import com.example.social_network.dtos.CommentDTO;
import com.example.social_network.dtos.NewCommentDTO;
import com.example.social_network.dtos.PostDTO;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.fragments.CommentsFragment;
import com.example.social_network.services.IPostService;
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

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private final List<PostDTO> posts;

    private final Context context;

    private final String token;

    private final boolean home;

    private final Long myId;

    private final Long userId;

    public PostsAdapter(List<PostDTO> posts, Context context, String token, boolean home, Long myId, Long userId) {
        this.posts = posts;
        this.context = context;
        this.token = token;
        this.home = home;
        this.myId = myId;
        this.userId = userId;
    }

    @NonNull
    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostsAdapter.ViewHolder holder, int position) {
        PostDTO item = posts.get(position);

        loadUsername(item.getUserId(), holder.textViewUsername);

        LocalDateTime dateTime = LocalDateTime.parse(item.getPostedAt(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));
        holder.textViewTimestamp.setText(dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        holder.textViewPostDescription.setText(item.getDescription());

        holder.textViewShowComments.setOnClickListener(view -> {
            holder.textViewShowComments.setVisibility(View.GONE);
            holder.frameLayoutFragment.setVisibility(View.VISIBLE);
            holder.textViewHideComments.setVisibility(View.VISIBLE);
            CommentsFragment fragment = new CommentsFragment(myId, item.getId());
            FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        });

        holder.textViewHideComments.setOnClickListener(view -> {
            holder.textViewShowComments.setVisibility(View.VISIBLE);
            holder.frameLayoutFragment.setVisibility(View.GONE);
            holder.textViewHideComments.setVisibility(View.GONE);
        });

        holder.imageViewDeletePost.setVisibility((home || !myId.equals(userId)) ? View.GONE : View.VISIBLE);

        holder.imageViewDeletePost.setOnClickListener(view -> showAreYouSureDialog(item.getId(), position));

        if (item.getFile() != null) {
            holder.imageViewPostImage.setVisibility(View.VISIBLE);
            loadImage(item.getId(), holder.imageViewPostImage);
        } else {
            holder.imageViewPostImage.setVisibility(View.GONE);
        }

        holder.buttonSubmitComment.setOnClickListener(view -> {
            String comment = holder.editTextComment.getText().toString();

            boolean hasError = false;

            if (comment.isEmpty()) {
                Log.e("Error", "Empty username");
                setError(holder.editTextComment);
                hasError = true;
            } else {
                resetError(holder.editTextComment);
            }

            if (!hasError) {
                NewCommentDTO newCommentDTO = new NewCommentDTO(myId, comment, null);
                IPostService postService = ServiceUtils.postService(token);
                Call<CommentDTO> call = postService.createComment(item.getId(), newCommentDTO);
                call.enqueue(new Callback<CommentDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<CommentDTO> call, @NonNull Response<CommentDTO> response) {
                        if (response.isSuccessful()) {
                            CommentDTO comment = response.body();
                            if (comment != null) {
                                Toast.makeText(context, "Successfully created comment!", Toast.LENGTH_SHORT).show();
                                holder.editTextComment.setText("");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CommentDTO> call, @NonNull Throwable t) {
                        Log.e("Error", "Creating comment failed.");
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUsername, textViewTimestamp, textViewPostDescription, textViewShowComments, textViewHideComments;

        ImageView imageViewDeletePost, imageViewPostImage;

        EditText editTextComment;

        Button buttonSubmitComment;

        FrameLayout frameLayoutFragment;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.username);
            textViewTimestamp = itemView.findViewById(R.id.timestamp);
            textViewPostDescription = itemView.findViewById(R.id.post_description);
            textViewShowComments = itemView.findViewById(R.id.show_comments);
            imageViewDeletePost = itemView.findViewById(R.id.delete_post);
            imageViewPostImage = itemView.findViewById(R.id.post_image);
            editTextComment = itemView.findViewById(R.id.comment_input);
            buttonSubmitComment = itemView.findViewById(R.id.submit_comment_button);
            frameLayoutFragment = itemView.findViewById(R.id.fragment_container);
            textViewHideComments = itemView.findViewById(R.id.hide_comments);
        }
    }

    private void showAreYouSureDialog(Long postId, int position) {
        final Dialog dialog = new Dialog(this.context);
        dialog.setContentView(R.layout.popup_are_you_sure);

        Button buttonYes = dialog.findViewById(R.id.yesButton);
        Button buttonNo = dialog.findViewById(R.id.noButton);

        buttonNo.setOnClickListener(v -> dialog.dismiss());

        buttonYes.setOnClickListener(v -> {
            deletePost(postId, position);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deletePost(Long postId, int position) {
        Call<Void> call = ServiceUtils.postService(token).delete(postId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    Toast.makeText(context, "Successfully deleted post!", Toast.LENGTH_SHORT).show();
                    posts.remove(position);
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

    private void loadImage(Long postId, ImageView imageView) {
        Call<ResponseBody> call = ServiceUtils.postService(token).downloadFile(postId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("Success", response.message());
                    assert response.body() != null;
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(bitmap);
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

    private void setError(EditText editText) {
        editText.setBackgroundResource(R.drawable.edit_text_error);
    }

    private void resetError(EditText editText) {
        editText.setBackgroundResource(R.drawable.edit_text);
    }

}
