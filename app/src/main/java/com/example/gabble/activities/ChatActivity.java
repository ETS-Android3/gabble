package com.example.gabble.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.gabble.R;
import com.example.gabble.databinding.ActivityChatBinding;
import com.example.gabble.databinding.ActivityUserBinding;
import com.example.gabble.models.User;
import com.example.gabble.utilities.Constants;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    private User receiverUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
        loadReceiverDetails();
    }

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

}