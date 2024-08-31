package com.example.social_network.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.R;
import com.example.social_network.services.ServiceUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostImagesAdapter  extends RecyclerView.Adapter<PostImagesAdapter.ViewHolder> {

    private final List<String> imagePaths;

    private final Context context;

    private final String token;

    public PostImagesAdapter(List<String> imagePaths, Context context, String token) {
        this.imagePaths = imagePaths;
        this.context = context;
        this.token = token;
    }

    @NonNull
    @Override
    public PostImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_image, parent, false);
        return new PostImagesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostImagesAdapter.ViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);

        loadImage(imagePath, holder.imageView);
    }

    private void loadImage(String imagePath, ImageView imageView) {
        Call<ResponseBody> call = ServiceUtils.postService(token).downloadImage(imagePath);
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

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewPostImage);
        }
    }

}
