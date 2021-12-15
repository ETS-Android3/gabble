package com.example.gabble.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gabble.listeners.UserListener;
import com.example.gabble.databinding.ItemContainerUserBinding;
import com.example.gabble.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    private final List<User> users;
    private final UserListener userListener;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
                );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;

        public UserViewHolder(@NonNull ItemContainerUserBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textAbout.setText(user.about);
            binding.imageProfile.setImageBitmap(decodeImage(user.image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }

        private Bitmap decodeImage(String sImage) {
            byte[] bytes = Base64.decode(sImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return bitmap;
        }

    }
}
