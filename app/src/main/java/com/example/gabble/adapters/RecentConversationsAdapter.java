package com.example.gabble.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gabble.databinding.ItemContainerRecentConversationBinding;
import com.example.gabble.listeners.ConversationListener;
import com.example.gabble.models.ChatMessage;
import com.example.gabble.models.User;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversationViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final ConversationListener conversationListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages,ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
          ItemContainerRecentConversationBinding.inflate(
                  LayoutInflater.from(parent.getContext()),
                  parent,
                  false
          )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversationBinding binding;

        ConversationViewHolder(ItemContainerRecentConversationBinding itemContainerRecentConversationBinding) {
            super(itemContainerRecentConversationBinding.getRoot());
            binding = itemContainerRecentConversationBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textName.setText(chatMessage.conversationName);
            binding.textRecentMessage.setText(chatMessage.message);

            if(chatMessage.conversationImage!=null) {
                binding.imageProfile.setImageBitmap(decodeImage(chatMessage.conversationImage));
            }

            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.name = chatMessage.conversationName;
                user.phoneNo = chatMessage.conversationId;
                conversationListener.onConversationClicked(user);
            });
        }

        private Bitmap decodeImage(String sImage) {
            byte[] bytes = Base64.decode(sImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return bitmap;
        }
    }

}
