package com.example.gabble.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

public class ImageConverter {

    public Bitmap decodeImage(String sImage) {
        byte[] bytes= Base64.decode(sImage,Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap;
    }

    public byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public void loadEncodedImage(Context pContext, String encodedImage, View imageView) {
        Bitmap pBitmap = decodeImage(encodedImage);
        try {
            Uri uri = Uri.fromFile(File.createTempFile("temp_file_name", ".jpg", pContext.getCacheDir()));
            OutputStream outputStream = pContext.getContentResolver().openOutputStream(uri);
            pBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            outputStream.close();
            Glide.with(pContext)
                    .load(uri)
                    .into((ImageView) imageView);
        } catch (Exception e) {
            Log.e("LoadBitmapByPicasso", e.getMessage());
        }
    }

    private String convertImage(Context context, Uri imageUri) {
        try {
            Bitmap bitmap= MediaStore.Images.Media.getBitmap(context.getContentResolver(),imageUri);
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,stream);
            byte[] bytes=stream.toByteArray();
            return Base64.encodeToString(bytes,Base64.DEFAULT);
        } catch (Exception e) {
            Log.d("demo", "convertImage: "+e.getMessage());
        }
        return null;
    }

}
