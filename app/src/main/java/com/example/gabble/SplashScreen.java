package com.example.gabble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.Inet4Address;
import java.util.Random;

public class SplashScreen extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();
        Handler handler = new Handler();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                if(firebaseUser!=null) {
                    handler.postDelayed(()->{
                        String mobileNo = firebaseUser.getPhoneNumber();
                        Intent i = new Intent(SplashScreen.this,chat.class);
                        i.putExtra("phoneNo",mobileNo);
                        startActivity(i);
                    },2500);
                }
                else{
                    handler.postDelayed(()->{
                        startActivity(new Intent(getApplicationContext(),sendOtp.class));
                    },2500);
                }
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }
}