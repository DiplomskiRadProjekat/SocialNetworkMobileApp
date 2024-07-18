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
import com.example.social_network.adapters.SearchResultsAdapter;
import com.example.social_network.dtos.UserDTO;
import com.example.social_network.services.ServiceUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultsFragment extends Fragment {

    private RecyclerView recyclerViewSearchResults;

    private SearchResultsAdapter searchResultsAdapter;

    private String token;

    private final String query;

    public SearchResultsFragment(String query) {
        this.query = query;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_list, container, false);

        recyclerViewSearchResults = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewSearchResults.setLayoutManager(layoutManager);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("pref_token", "");

        searchUsers();
        recyclerViewSearchResults.setAdapter(searchResultsAdapter);

        return view;
    }

    private void searchUsers() {
        if (query.isEmpty()) {
            List<UserDTO> usersEmpty = new ArrayList<>();
            SearchResultsAdapter adapter = new SearchResultsAdapter(usersEmpty, getContext());
            recyclerViewSearchResults.setAdapter(adapter);
        } else {
            Call<List<UserDTO>> call = ServiceUtils.userService(token).searchUsers(query);

            call.enqueue(new Callback<List<UserDTO>>() {
                @Override
                public void onResponse(@NonNull Call<List<UserDTO>> call, @NonNull Response<List<UserDTO>> response) {
                    if (response.isSuccessful()) {
                        List<UserDTO> users = response.body();
                        SearchResultsAdapter adapter = new SearchResultsAdapter(users, getContext());
                        recyclerViewSearchResults.setAdapter(adapter);
                    } else {
                        Log.e("API Error", "Failed to fetch users: " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<UserDTO>> call, @NonNull Throwable t) {
                    Log.e("API Error", "Failed to fetch users", t);
                }
            });
        }
    }

}
