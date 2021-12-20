package com.example.gabble.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.gabble.R;
import com.example.gabble.activities.BaseActivity;
import com.example.gabble.adapters.StoryAdapter;
import com.example.gabble.databinding.ActivityStoryBinding;
import com.example.gabble.listeners.StoryListener;
import com.example.gabble.models.Story;
import com.example.gabble.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoryActivity extends BaseActivity implements StoryListener {

    private ActivityStoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
        getStories();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        });

        binding.fabMyStory.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),CreateStoryActivity.class));
        });
    }

    private void getStories() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_STORIES).get()
                .addOnCompleteListener(task -> {
                    loading(false);

                    if(task.isSuccessful() && task.getResult()!=null) {
                        List<Story> stories = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            Story story = new Story();

                            story.senderName = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            story.senderNo = queryDocumentSnapshot.getString(Constants.KEY_MOBILE);
                            story.dateObject =
                                    queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP);
                            story.profileImage =
                                    queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            story.textMessage =
                                    queryDocumentSnapshot.getString(Constants.KEY_MESSAGE);
                            story.fontFamilyId =
                                    queryDocumentSnapshot.getString(Constants.STORY_FONT_FAMILY_ID);

                            if(story.senderNo.equals(new SendOtp().getMobileNo())) {
                                story.senderName = getResources().getString(R.string.my_story);
                            }

                            if(!checkStoryValidity(story.dateObject)) {
                                continue;
                            }
                            stories.add(story);
                        }
                        if(stories.size()>0) {
                            StoryAdapter storyAdapter = new StoryAdapter(stories,this);
                            binding.storyRecyclerView.setAdapter(storyAdapter);
                            binding.storyRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    /*
        This function checks whether 24 hrs have been elapsed since the story was posted.
     */
    private boolean checkStoryValidity(Date date) {
        Date curr_date = new Date();
        long ms = curr_date.getTime() - date.getTime();
        ms = ms/1000;

        Log.d(Constants.TAG, "checkStoryValidity: "+ms);

        return ms <= 86400;
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s","No Story Updates"));
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
    public void onStoryClicked(Story story) {
        Intent intent = new Intent(getApplicationContext(),StorySlidePagerActivity.class);
        intent.putExtra(Constants.KEY_STORY,story);
        startActivity(intent);
    }
}