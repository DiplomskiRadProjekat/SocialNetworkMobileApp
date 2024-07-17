package com.example.social_network.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.R;
import com.example.social_network.adapters.PostsAdapter;
import com.example.social_network.dtos.PostDTO;
import com.example.social_network.services.ServiceUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostsFragment extends Fragment {

    private RecyclerView recyclerViewPosts;

    private PostsAdapter postsAdapter;

    private final Long userId;

    private final Long myId;

    private String token;

    private final boolean home;

    public PostsFragment(boolean home, Long myId, Long userId) {
        this.home = home;
        this.myId = myId;
        this.userId = userId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_items_list, container, false);

        recyclerViewPosts = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewPosts.setLayoutManager(layoutManager);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("pref_token", "");

        if (home) {
            getHomePagePosts();
        } else {
            getUserPosts();
        }
        recyclerViewPosts.setAdapter(postsAdapter);

        return view;
    }

    private void getHomePagePosts() {
        Call<List<PostDTO>> call = ServiceUtils.userService(token).getAllFriendsPosts(userId);

        call.enqueue(new Callback<List<PostDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<PostDTO>> call, @NonNull Response<List<PostDTO>> response) {
                if (response.isSuccessful()) {
                    List<PostDTO> friendsPosts = response.body();
                    PostsAdapter adapter = new PostsAdapter(friendsPosts, getContext(), token, home, myId, userId);
                    recyclerViewPosts.setAdapter(adapter);
                } else {
                    Log.e("API Error", "Failed to fetch friends posts: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PostDTO>> call, @NonNull Throwable t) {
                Log.e("API Error", "Failed to fetch friends posts", t);
            }
        });
    }

    private void getUserPosts() {
        Call<List<PostDTO>> call = ServiceUtils.userService(token).getAllPosts(userId);

        call.enqueue(new Callback<List<PostDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<PostDTO>> call, @NonNull Response<List<PostDTO>> response) {
                if (response.isSuccessful()) {
                    List<PostDTO> posts = response.body();
                    PostsAdapter adapter = new PostsAdapter(posts, getContext(), token, home, myId, userId);
                    recyclerViewPosts.setAdapter(adapter);
                } else {
                    Log.e("API Error", "Failed to fetch posts: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PostDTO>> call, @NonNull Throwable t) {
                Log.e("API Error", "Failed to fetch posts", t);
            }
        });
    }

}
