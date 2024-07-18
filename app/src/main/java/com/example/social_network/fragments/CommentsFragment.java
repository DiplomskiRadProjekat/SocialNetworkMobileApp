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
import com.example.social_network.adapters.CommentsAdapter;
import com.example.social_network.dtos.CommentDTO;
import com.example.social_network.services.ServiceUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsFragment extends Fragment {

    private RecyclerView recyclerViewComments;

    private CommentsAdapter commentsAdapter;

    private final Long myId;

    private final Long postId;

    private String token;

    public CommentsFragment(Long myId, Long postId) {
        this.myId = myId;
        this.postId = postId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_items_list, container, false);

        recyclerViewComments = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerViewComments.setLayoutManager(layoutManager);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("pref_token", "");

        getComments();
        recyclerViewComments.setAdapter(commentsAdapter);

        return view;
    }

    private void getComments() {
        Call<List<CommentDTO>> call = ServiceUtils.postService(token).getAllComments(postId);

        call.enqueue(new Callback<List<CommentDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<CommentDTO>> call, @NonNull Response<List<CommentDTO>> response) {
                if (response.isSuccessful()) {
                    List<CommentDTO> comments = response.body();
                    CommentsAdapter adapter = new CommentsAdapter(comments, getContext(), token, myId);
                    recyclerViewComments.setAdapter(adapter);
                } else {
                    Log.e("API Error", "Failed to fetch comments: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CommentDTO>> call, @NonNull Throwable t) {
                Log.e("API Error", "Failed to fetch comments", t);
            }
        });
    }

}
