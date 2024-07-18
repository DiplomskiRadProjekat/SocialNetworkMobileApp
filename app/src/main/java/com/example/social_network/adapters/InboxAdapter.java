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

import com.example.social_network.ChatActivity;
import com.example.social_network.R;
import com.example.social_network.dtos.UserDTO;

import java.util.List;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private final List<UserDTO> friends;

    private final Context context;

    public InboxAdapter(List<UserDTO> friends, Context context) {
        this.friends = friends;
        this.context = context;
    }

    @NonNull
    @Override
    public InboxAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_chat, parent, false);
        return new InboxAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InboxAdapter.ViewHolder holder, int position) {
        UserDTO item = friends.get(position);

        holder.textViewUsername.setText(String.format("@%s", item.getUsername()));

        holder.linearLayoutProfile.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("friendId", item.getUid());
            intent.putExtra("friendUsername", item.getUsername());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUsername;

        LinearLayout linearLayoutProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.username);
            linearLayoutProfile = itemView.findViewById(R.id.profile);
        }
    }

}
