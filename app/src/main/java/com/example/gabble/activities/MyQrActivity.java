package com.example.gabble.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.gabble.R;
import com.example.gabble.databinding.ActivityMyQrBinding;
import com.example.gabble.utilities.Constants;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MyQrActivity extends AppCompatActivity {

    private ActivityMyQrBinding binding;
    private SharedPreferences sharedPreferences;
    private String mobile;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
        getUserDetails();
        setQrCode();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v-> {
            onBackPressed();
        });
    }

    private void getUserDetails() {
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,MODE_PRIVATE);
        mobile = sharedPreferences.getString(Constants.KEY_MOBILE,null);
    }

    private void setQrCode() {
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        QRGEncoder qrgEncoder = new QRGEncoder(mobile, null, QRGContents.Type.TEXT, smallerDimension);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);

        binding.qrImage.setVisibility(View.VISIBLE);

        bitmap = qrgEncoder.getBitmap();
        binding.qrImage.setImageBitmap(bitmap);
    }

}