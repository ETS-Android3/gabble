package com.example.gabble.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.gabble.R;
import com.example.gabble.databinding.ActivityReceiverProfileBinding;
import com.example.gabble.models.User;
import com.example.gabble.utilities.Constants;
import com.example.gabble.utilities.GetUserInformation;
import com.example.gabble.utilities.ImageConverter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReceiverProfileActivity extends AppCompatActivity {

    private User receiverUser;
    private ActivityReceiverProfileBinding binding;
    private String about=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiverProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListeners();
        loadUserDetails();
    }

    private void init() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.phoneNo);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try{
                    about = documentSnapshot.getString(Constants.KEY_ABOUT).toString();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage().toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setListeners() {
        binding.profileDiscard.setOnClickListener(v -> onBackPressed());

        binding.saveProfile.setOnClickListener(v -> onBackPressed());
    }

    private void loadUserDetails() {
        binding.profileName.setText(receiverUser.name);
        binding.profileAbout.setText(about);
        Log.d(Constants.TAG, "loadUserDetails: "+about);
        new ImageConverter().loadEncodedImage(getApplicationContext(), receiverUser.image,
                binding.profileImage);
    }


}