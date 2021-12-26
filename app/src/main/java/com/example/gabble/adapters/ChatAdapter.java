package com.example.gabble.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gabble.databinding.ItemContainerReceivedMessageBinding;
import com.example.gabble.databinding.ItemContainerSentMessageBinding;
import com.example.gabble.glide.GlideApp;
import com.example.gabble.models.ChatMessage;
import com.example.gabble.utilities.Constants;
import com.example.gabble.utilities.ImageConverter;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final String senderNo;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, String senderNo) {
        this.chatMessages = chatMessages;
        this.senderNo = senderNo;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderNo.equals(senderNo)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerSentMessageBinding binding;

            SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
                super(itemContainerSentMessageBinding.getRoot());
                binding = itemContainerSentMessageBinding;
            }

            void setData(ChatMessage chatMessage) {
                Log.d(Constants.TAG, "setData: "+chatMessage.messageType);

                if(chatMessage.messageType!=null) {
                    if (chatMessage.messageType.equals(Constants.KEY_TYPE_IMAGE)) {
                        binding.textMessage.setVisibility(View.GONE);
                        binding.imageMessage.setVisibility(View.VISIBLE);
                        new ImageConverter().loadEncodedImage(itemView.getContext(), chatMessage.message,
                                binding.imageMessage);
                    } else {
                        binding.textMessage.setVisibility(View.VISIBLE);
                        binding.imageMessage.setVisibility(View.GONE);
                        binding.textMessage.setText(chatMessage.message);
                    }
                }

                binding.textDateTime.setText(chatMessage.dateTime);
            }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage) {

            if(chatMessage.messageType!=null) {
                if (chatMessage.messageType.equals(Constants.KEY_TYPE_IMAGE)) {
                    binding.imageMessage.setVisibility(View.VISIBLE);
                    binding.textMessage.setVisibility(View.GONE);
                    binding.imageMessage.setImageBitmap(new ImageConverter().decodeImage(chatMessage.message));
                } else {
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.imageMessage.setVisibility(View.GONE);
                    binding.textMessage.setText(chatMessage.message);
                }
            }

            binding.textDateTime.setText(chatMessage.dateTime);
        }

    }

}
