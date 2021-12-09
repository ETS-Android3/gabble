package com.example.gabble.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.gabble.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SendOtp extends AppCompatActivity {

    private String mobileNo;
    private String TAG = "otp";
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    Button send;
    EditText number;
     // this thing needs to be added to splash
    // screen to avoid sendotp screen from displaying.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);

        number = findViewById(R.id.number);
        send = findViewById(R.id.send);
        progressBar = findViewById(R.id.pg1);

        mAuth = FirebaseAuth.getInstance();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileNo = number.getText().toString();

                progressBar.setVisibility(View.VISIBLE);
                send.setVisibility(View.INVISIBLE);

                sendVerificationCodeToUser();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        @SuppressLint("WrongConstant") SharedPreferences sh = getSharedPreferences("userdata",MODE_APPEND);
        mobileNo = sh.getString("mobile","");
        number.setText(mobileNo);
    }

    private void sendVerificationCodeToUser() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+mobileNo)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Intent intent = new Intent(SendOtp.this, ReceiveOtp.class);
            intent.putExtra("phoneNo",mobileNo);
            intent.putExtra("backendOtp",s);
            startActivity(intent);
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            progressBar.setVisibility(View.INVISIBLE);
            send.setVisibility(View.VISIBLE);

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            progressBar.setVisibility(View.INVISIBLE);
            send.setVisibility(View.VISIBLE);
            Log.d(TAG, "onVerificationFailed: "+e.getMessage());
            Toast.makeText(SendOtp.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    public String getMobileNo(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            mobileNo = firebaseUser.getPhoneNumber();
        }
        return mobileNo.substring(3);
    }

}