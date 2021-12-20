package com.example.gabble.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.gabble.fragments.StorySlidePageFragment;
import com.example.gabble.models.Story;

import java.util.List;

public class StorySlidePagerAdapter extends FragmentStateAdapter {

    private StorySlidePageFragment storySlidePageFragment;
    private List<Story> stories;

    public StorySlidePagerAdapter(List<Story> stories,@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.stories = stories;
    }

    public StorySlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public StorySlidePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public StorySlidePagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new StorySlidePageFragment(stories.get(position).textMessage,
                stories.get(position).fontFamilyId);
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }
}
