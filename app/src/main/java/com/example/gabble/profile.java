package com.example.gabble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.example.gabble.sendOtp;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class profile extends AppCompatActivity {

    ImageView profile_image;
    EditText profile_name;
    Button save_profile;
    private String name;
    private String mobileNo;
    FirebaseFirestore db;
    DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile_image = findViewById(R.id.profile_image);
        profile_name = findViewById(R.id.profile_name);
        save_profile = findViewById(R.id.save_profile);

        mobileNo = new sendOtp().getMobileNo();
        db = FirebaseFirestore.getInstance();
        documentReference = db.collection("users").document(mobileNo);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                name = documentSnapshot.get("name").toString();
                profile_name.setText(name);
            }
        });

        save_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = profile_name.getText().toString();
                updateDB();
                startActivity(new Intent(getApplicationContext(),chat.class));
            }
        });
    }

    private void updateDB() {
        Map<String, String> user = new HashMap<>();
        user.put("name",name);
        db.collection("users").document(mobileNo).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("profile", "onSuccess: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("profile", "onFailure: "+e.getMessage());
            }
        });
    }

    public String getName() {
        return name;
    }

    public void setProfile_image() {
        Random rand = new Random();
        String url = "https://avatars.dicebear.com/api/open-peeps/"+rand.nextInt(1000)+".svg";
    }

}