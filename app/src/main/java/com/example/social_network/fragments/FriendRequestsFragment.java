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
import com.example.social_network.adapters.FriendRequestsAdapter;
import com.example.social_network.dtos.FriendRequestDTO;
import com.example.social_network.dtos.FriendRequestStatus;
import com.example.social_network.services.ServiceUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendRequestsFragment extends Fragment {

    private RecyclerView recyclerViewFriendRequests;

    private FriendRequestsAdapter friendRequestsAdapter;

    private final Long userId;

    private String token;

    private Long myId;

    public FriendRequestsFragment(Long userId) {
        this.userId = userId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_items_list, container, false);

        recyclerViewFriendRequests = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewFriendRequests.setLayoutManager(layoutManager);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("pref_token", "");
        myId = sharedPreferences.getLong("pref_id", 0);

        getAllFriends();
        recyclerViewFriendRequests.setAdapter(friendRequestsAdapter);

        return view;
    }

    private void getAllFriends() {
        Call<List<FriendRequestDTO>> call = ServiceUtils.userService(token).getAllFriendRequests(userId, FriendRequestStatus.PENDING);

        call.enqueue(new Callback<List<FriendRequestDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<FriendRequestDTO>> call, @NonNull Response<List<FriendRequestDTO>> response) {
                if (response.isSuccessful()) {
                    List<FriendRequestDTO> friendRequests = response.body();
                    List<FriendRequestDTO> myFriendRequests = new ArrayList<>();
                    for (FriendRequestDTO fr : friendRequests) {
                        if (fr.getToUserId().equals(myId)) {
                            myFriendRequests.add(fr);
                        }
                    }
                    FriendRequestsAdapter adapter = new FriendRequestsAdapter(myFriendRequests, getContext(), token);
                    recyclerViewFriendRequests.setAdapter(adapter);
                } else {
                    Log.e("API Error", "Failed to fetch accommodations: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FriendRequestDTO>> call, @NonNull Throwable t) {
                Log.e("API Error", "Failed to fetch friend requests", t);
            }
        });
    }

}
