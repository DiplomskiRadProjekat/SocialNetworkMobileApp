package com.example.social_network.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.R;
import com.example.social_network.dtos.MessageModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;

    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<MessageModel> messages  = new ArrayList<>();

    private Context context;

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void add(MessageModel messageModel) {
        messages.add(messageModel);
        notifyDataSetChanged();
    }

    public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType==VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        MessageModel item = messages.get(position);

        if (item.getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            holder.textViewSentMessage.setText(item.getMessage());
        } else {
            holder.textViewReceivedMessage.setText(item.getMessage());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public List<MessageModel> getAllMessages() {
        return messages;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewSentMessage, textViewReceivedMessage;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewSentMessage = itemView.findViewById(R.id.sentMessage);
            textViewReceivedMessage = itemView.findViewById(R.id.receivedMessage);
        }
    }

}
