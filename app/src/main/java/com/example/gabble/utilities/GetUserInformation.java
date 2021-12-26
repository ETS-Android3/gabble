package com.example.gabble.utilities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.gabble.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/*
    This class fetches user information from the database according to the
    mobile number that is provided while calling the constructor of this class.
*/

public class GetUserInformation {

    private String mobileNo;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private User user;
    private String about;

    public GetUserInformation(String mobileNo) {
        this.mobileNo = mobileNo;
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference =
                firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS).document(mobileNo);
        user = new User();
        setListeners();
    }

    private void setListeners() {
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user.name = documentSnapshot.getString(Constants.KEY_NAME);
                user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                user.about = documentSnapshot.getString(Constants.KEY_ABOUT);
                about = documentSnapshot.getString(Constants.KEY_ABOUT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(Constants.TAG, "onFailure: ");
            }
        });
    }

    public String getName() {
        return user.name;
    }

    public String getAbout() {
        return about;
    }

    public String getImage() {
        return user.image;
    }
}
