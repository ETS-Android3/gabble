package com.example.gabble.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.gabble.MainActivity;
import com.example.gabble.R;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    ImageView profile_image;
    EditText profile_name;
    Button save_profile;
    private String name;
    private String mobileNo;
    FloatingActionButton pickImage;
    FirebaseFirestore db;
    DocumentReference documentReference;

    public static final int PICK_IMAGE = 1;
    public static final int PIC_CROP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile_image = findViewById(R.id.profile_image);
        profile_name = findViewById(R.id.profile_name);
        save_profile = findViewById(R.id.save_profile);
        pickImage = findViewById(R.id.updateImage);

        mobileNo = new SendOtp().getMobileNo();
        db = FirebaseFirestore.getInstance();
        documentReference = db.collection("users").document(mobileNo);

        getProfileImage();
        setListeners();
    }

    private void getProfileImage() {
        StorageReference storageReference =
                FirebaseStorage.getInstance().getReference("users/"+mobileNo+"/profile.jpg");
        if(storageReference!=null) {
            Toast.makeText(ProfileActivity.this, "fetched image", Toast.LENGTH_SHORT).show();
            Glide.with(this)
                    .load(storageReference)
                    .into(profile_image);
        }

    }

    private void setListeners() {
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
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra("crop","true");
                intent.putExtra("scale",true);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            Uri selectedImageUri = data.getData();
            if(selectedImageUri!=null) {
                profile_image.setImageURI(selectedImageUri);
                uploadImage(selectedImageUri);
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        StorageReference userReference = storageReference.child("users/"+mobileNo+"/profile.jpg");
        UploadTask uploadTask = userReference.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ProfileActivity.this, "Success!", Toast.LENGTH_SHORT).show();
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

    // if time left -> figure this out
    public void setProfile_image() {
        Random rand = new Random();
        String url = "https://avatars.dicebear.com/api/open-peeps/"+rand.nextInt(1000)+".svg";
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getSharedPreferences("userdata",MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        myEdit.putString("name",name);
        myEdit.putString("mobile",mobileNo);

        myEdit.commit();
    }
}