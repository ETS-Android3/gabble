package com.example.gabble.fragments;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gabble.R;

public class StorySlidePageFragment extends Fragment {

    public String text;
    public String fontFamilyId;
    private TextView textView;

    public StorySlidePageFragment(String text,String fontFamilyId) {
        this.text = text;
        this.fontFamilyId = fontFamilyId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_story_slide_page, container, false);
        textView = root.findViewById(R.id.story_content_text);
        updateFontStyle();
        textView.setText(text);
        return root;
    }

    private void updateFontStyle() {
        if(fontFamilyId.equals("0")) {
            return;
        }
        Typeface face = ResourcesCompat.getFont(getContext(),Integer.parseInt(fontFamilyId));
        textView.setTypeface(face);
    }
}