package com.example.gabble.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.gabble.listeners.UserListener;
import com.example.gabble.adapters.UsersAdapter;
import com.example.gabble.databinding.ActivityUserBinding;
import com.example.gabble.models.User;
import com.example.gabble.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener {

    private ActivityUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        getUsers();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String mobileNo = new SendOtp().getMobileNo();
                    if(task.isSuccessful() && task.getResult()!=null) {
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if(mobileNo.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.about = queryDocumentSnapshot.getString(Constants.KEY_ABOUT);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);

                            user.phoneNo = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size()>0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecylerView.setAdapter(usersAdapter);
                            binding.usersRecylerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s","No User available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}