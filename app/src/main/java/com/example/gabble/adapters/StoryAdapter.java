package com.example.gabble.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gabble.databinding.ItemContainerStoryBinding;
import com.example.gabble.listeners.StoryListener;
import com.example.gabble.models.Story;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder>{

    private final List<Story> stories;
    private final StoryListener storyListener;

    public StoryAdapter(List<Story> stories, StoryListener storyListener) {
        this.stories = stories;
        this.storyListener = storyListener;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerStoryBinding itemContainerStoryBinding = ItemContainerStoryBinding.inflate(
          LayoutInflater.from(parent.getContext()),
                parent,false
        );
        return new StoryViewHolder(itemContainerStoryBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        holder.setStoryData(stories.get(position));
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    class StoryViewHolder extends RecyclerView.ViewHolder {
        ItemContainerStoryBinding binding;

        public StoryViewHolder(@NonNull ItemContainerStoryBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        void setStoryData(Story story) {
            binding.textName.setText(story.senderName);
            binding.imageProfile.setImageBitmap(decodeImage(story.profileImage));
            binding.textDateTime.setText(getReadableDateTime(story.dateObject));
            binding.getRoot().setOnClickListener(v -> storyListener.onStoryClicked(story));
        }

        private Bitmap decodeImage(String sImage) {
            byte[] bytes = Base64.decode(sImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return bitmap;
        }

        private String getReadableDateTime(Date date) {
            return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
        }
    }

}
