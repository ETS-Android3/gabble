package com.example.gabble.activities;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.gabble.R;
import com.example.gabble.adapters.StorySlidePagerAdapter;
import com.example.gabble.fragments.StorySlidePageFragment;
import com.example.gabble.models.Story;
import com.example.gabble.utilities.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.List;

public class StorySlidePagerActivity extends FragmentActivity {

    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_slide);

        viewPager = findViewById(R.id.storyPager);
        getStoryDetails();

        getSlidr();
    }

    private void getSlidr() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.TOP)
                .build();
        Slidr.attach(this,config);
    }

    private void getStoryDetails() {
        Story storyObject = (Story) getIntent().getSerializableExtra(Constants.KEY_STORY);
        List<Story> stories = new ArrayList<>();
        stories.add(storyObject);
        StorySlidePagerAdapter adapter = new StorySlidePagerAdapter(stories,this);
        viewPager.setAdapter(adapter);
    }

}
