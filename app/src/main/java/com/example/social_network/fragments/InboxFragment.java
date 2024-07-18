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
import com.example.social_network.adapters.InboxAdapter;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.services.ServiceUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InboxFragment extends Fragment {

    private RecyclerView recyclerViewFriends;

    private InboxAdapter inboxAdapter;

    private final Long myId;

    private String token;

    public InboxFragment(Long myId) {
        this.myId = myId;
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
        recyclerViewFriends.setAdapter(inboxAdapter);

        return view;
    }

    private void getAllFriends() {
        Call<List<UserDTO>> call = ServiceUtils.userService(token).getAllFriends(myId);

        call.enqueue(new Callback<List<UserDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserDTO>> call, @NonNull Response<List<UserDTO>> response) {
                if (response.isSuccessful()) {
                    List<UserDTO> friends = response.body();
                    InboxAdapter adapter = new InboxAdapter(friends, getContext());
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
