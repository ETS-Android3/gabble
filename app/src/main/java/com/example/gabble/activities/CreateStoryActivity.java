package com.example.gabble.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gabble.R;
import com.example.gabble.databinding.ActivityCreateStoryBinding;
import com.example.gabble.models.Story;
import com.example.gabble.utilities.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.HashMap;

import jp.wasabeef.richeditor.RichEditor;

public class CreateStoryActivity extends AppCompatActivity {

    private ActivityCreateStoryBinding binding;
    private String fontFamily;
    private int counter = 0;
    int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EmojiManager.install(new GoogleEmojiProvider());

        showKeyboard();
        setListeners();
    }

    // to set Keyboard Visibility
    private void showKeyboard() {
        binding.getRoot().post(
                new Runnable() {
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInputFromWindow(binding.storyContent.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                        binding.storyContent.requestFocus();
                    }
                });
    }

    private void setListeners() {
        // Emoji keyboard
        EmojiPopup popup =
                EmojiPopup.Builder.fromRootView(binding.getRoot()).build(binding.storyContent);
        binding.emoji.setOnClickListener(v -> {
            popup.toggle();
        });

        // button to change font family
        binding.fontStyle.setOnClickListener(v -> {
            updateFontStyle();
        });

        // to enable/disable Floating action button
        binding.storyContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    binding.fabCreateStory.setVisibility(View.GONE);
                } else {
                    binding.fabCreateStory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Send button action
        binding.fabCreateStory.setOnClickListener(v -> {
            uploadStory();
        });
    }

    /*
        This function updates the font style
     */
    private void updateFontStyle() {
        Typeface face;
        id = R.font.fuzzy;

        switch (counter) {
            case 0 :
                id=R.font.fuzzy;break;
            case 1 :
                id=R.font.avocado;break;
            case 2 :
                id = R.font.bomb;break;
            case 3 :
                id = R.font.pacifico;break;
            default:
                counter = -1;
        }
        face = ResourcesCompat.getFont(getApplicationContext(),id);
        binding.storyContent.setTypeface(face);
        counter++;
    }

    /*
        This function uploads the story in the database.
     */
    private void uploadStory() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE);

        HashMap<String,Object> story = new HashMap<>();
        story.put(Constants.KEY_MOBILE,sharedPreferences.getString(Constants.KEY_MOBILE, ""));
        story.put(Constants.KEY_NAME,sharedPreferences.getString(Constants.KEY_NAME, ""));
        story.put(Constants.KEY_IMAGE,sharedPreferences.getString(Constants.KEY_IMAGE, ""));
        story.put(Constants.KEY_MESSAGE,binding.storyContent.getText().toString());
        story.put(Constants.STORY_TYPE,Constants.STORY_TYPE_TEXT);
        story.put(Constants.KEY_TIMESTAMP,new Date());
        story.put(Constants.STORY_FONT_FAMILY_ID,Integer.toString(id));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_STORIES).add(story).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                startActivity(new Intent(getApplicationContext(),StoryActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "No internet!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}