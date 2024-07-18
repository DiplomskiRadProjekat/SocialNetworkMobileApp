package com.example.social_network.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.ProfileActivity;
import com.example.social_network.R;
import com.example.social_network.dtos.UserDTO;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private List<UserDTO> users;

    private final Context context;

    public SearchResultsAdapter(List<UserDTO> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserDTO item = users.get(position);

        holder.textViewUsername.setText(String.format("@%s", item.getUsername()));

        holder.textViewName.setText(String.format("%s %s", item.getFirstName(), item.getLastName()));

        holder.linearLayoutProfile.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userId", item.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUsername, textViewName;

        LinearLayout linearLayoutProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.username);
            textViewName = itemView.findViewById(R.id.name);
            linearLayoutProfile = itemView.findViewById(R.id.profile);
        }
    }

}
