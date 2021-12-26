package com.example.gabble.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gabble.R;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.gabble.databinding.ActivityProfileBinding;
import com.example.gabble.glide.GlideApp;
import com.example.gabble.utilities.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// add comments describing what each function does

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;

    private String name;
    private String mobileNo;
    private String encodedImage;
    private String about;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private DocumentReference documentReference;

    public static final int PICK_IMAGE = 1;
    public static final int PIC_CROP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mobileNo = new SendOtp().getMobileNo();
        db = FirebaseFirestore.getInstance();
        documentReference = db.collection(Constants.KEY_COLLECTION_USERS).document(mobileNo);

        getSharedData();
        setDiscardButton();
        setListeners();
    }

    private void getSharedData() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,MODE_PRIVATE);
        encodedImage = sharedPreferences.getString(Constants.KEY_IMAGE,null);
        name = sharedPreferences.getString(Constants.KEY_NAME,"");
        about = sharedPreferences.getString(Constants.KEY_ABOUT,Constants.DEFAULT_ABOUT);

        if(encodedImage!=null) {
            binding.profileImage.setImageBitmap(decodeImage(encodedImage));
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_blank_profile);
            encodedImage =
                    convertImage(Uri.parse("android.resource://com.example.gabble/"+R.drawable.ic_blank_profile));
        }

        if(!name.equals("")) {
            binding.profileName.setText(name);
        }

        binding.profileAbout.setText(about);
    }

    private void setDiscardButton() {
        if(getIntent().getStringExtra(Constants.COMING_FROM_WHICH_ACTIVITY).equals(Constants.OTP_ACTIVITY))
            binding.profileDiscard.setVisibility(View.INVISIBLE);
    }

    private void setListeners() {
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try {
                    name = documentSnapshot.get(Constants.KEY_NAME).toString();
                    encodedImage = documentSnapshot.get(Constants.KEY_IMAGE).toString();
                    about = documentSnapshot.get(Constants.KEY_ABOUT).toString();
                    binding.profileName.setText(name);
                    binding.profileImage.setImageBitmap(decodeImage(encodedImage));
                    binding.profileAbout.setText(about);
                } catch (Exception e) {

                }
            }
        });

        binding.saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = binding.profileName.getText().toString();
                about = binding.profileAbout.getText().toString();
                updateDB();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        binding.updateImage.setOnClickListener(new View.OnClickListener() {
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

        binding.profileDiscard.setOnClickListener(v-> onBackPressed());
    }

    private String convertImage(Uri imageUri) {
        try {
            Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,stream);
            byte[] bytes=stream.toByteArray();
            return Base64.encodeToString(bytes,Base64.DEFAULT);
        } catch (Exception e) {
            Log.d("demo", "convertImage: "+e.getMessage());
        }
        return null;
    }

    private Bitmap decodeImage(String sImage) {
        byte[] bytes=Base64.decode(sImage,Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap;
    }

    private void uploadImage(Uri imageUri) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        StorageReference userReference = storageReference.child("users/"+mobileNo+"/profile.jpg");
        UploadTask uploadTask = userReference.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(ProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                Toast.makeText(ProfileActivity.this, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            selectedImageUri = data.getData();
            if(selectedImageUri!=null) {
                binding.profileImage.setImageURI(selectedImageUri);
                encodedImage = convertImage(selectedImageUri);
                Log.d(Constants.TAG, "onActivityResult: "+encodedImage);
            } else {

            }
        }
    }

    private void updateDB() {
        Map<String, String> user = new HashMap<>();
        user.put(Constants.KEY_NAME,name);
        user.put(Constants.KEY_MOBILE,mobileNo);
        user.put(Constants.KEY_IMAGE,encodedImage);
        user.put(Constants.KEY_ABOUT,about);
        db.collection(Constants.KEY_COLLECTION_USERS).document(mobileNo).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        myEdit.putString(Constants.KEY_NAME,name);
        myEdit.putString(Constants.KEY_MOBILE,mobileNo);
        myEdit.putString(Constants.KEY_IMAGE,encodedImage);
        myEdit.putString(Constants.KEY_ABOUT,about);

        myEdit.commit();
    }

}