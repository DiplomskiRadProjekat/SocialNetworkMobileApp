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
import com.example.social_network.adapters.FriendsAdapter;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.services.ServiceUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsFragment extends Fragment {

    private RecyclerView recyclerViewFriends;

    private FriendsAdapter friendsAdapter;

    private final Long userId;

    private String token;

    private final boolean myProfile;

    public FriendsFragment(Long userId, boolean myProfile) {
        this.userId = userId;
        this.myProfile = myProfile;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_items_list, container, false);

        recyclerViewFriends = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewFriends.setLayoutManager(layoutManager);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("pref_token", "");

        getAllFriends();
        recyclerViewFriends.setAdapter(friendsAdapter);

        return view;
    }

    private void getAllFriends() {
        Call<List<UserDTO>> call = ServiceUtils.userService(token).getAllFriends(userId);

        call.enqueue(new Callback<List<UserDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserDTO>> call, @NonNull Response<List<UserDTO>> response) {
                if (response.isSuccessful()) {
                    List<UserDTO> friends = response.body();
                    FriendsAdapter adapter = new FriendsAdapter(friends, getContext(), token, userId, myProfile);
                    recyclerViewFriends.setAdapter(adapter);
                } else {
                    Log.e("API Error", "Failed to fetch friends: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserDTO>> call, @NonNull Throwable t) {
                Log.e("API Error", "Failed to fetch friends", t);
            }
        });
    }

}
