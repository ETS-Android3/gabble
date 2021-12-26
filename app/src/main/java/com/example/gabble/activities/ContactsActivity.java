package com.example.gabble.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import com.example.gabble.R;
import com.example.gabble.adapters.ContactsAdapter;
import com.example.gabble.databinding.ActivityContactsBinding;
import com.example.gabble.listeners.ContactListener;
import com.example.gabble.models.Contact;
import com.example.gabble.models.User;
import com.example.gabble.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity implements ContactListener {

    private ActivityContactsBinding binding;
    private User receiverUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);

        setListeners();
        getContacts();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getContacts() {
        loading(false);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_CONTACTS},0);
        }

        ContentResolver contentResolver = getContentResolver();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC";
        Cursor cursor = contentResolver.query(uri,null,null,null,
                sort);

        List<Contact> contactList = new ArrayList<>();

        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String id =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String contactName =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ " =?";
                Cursor phoneCursor = getContentResolver().query(phoneUri,null,selection,
                        new String[]{id},null);

                if(phoneCursor.moveToNext()) {
                    @SuppressLint("Range") String contactNumber =
                            phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    Contact contact = new Contact();
                    contact.name = contactName;
                    contact.mobile = contactNumber;
                    Log.d(Constants.TAG, "getContacts: "+contactName+" "+contactNumber);
                    contactList.add(contact);
                    phoneCursor.close();
                }
            }
            cursor.close();
        }

        if(contactList.size()>0) {
            ContactsAdapter contactsAdapter = new ContactsAdapter(contactList,this);
            binding.contactsRecylerView.setAdapter(contactsAdapter);
            binding.contactsRecylerView.setVisibility(View.VISIBLE);
        }
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onContactClicked(Contact contact) {
        String message = "Name : "+contact.name+"\n"+"Mobile : "+contact.mobile;

        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,receiverUser);
        intent.putExtra(Constants.KEY_CONTACT_MESSAGE,message);

        startActivity(intent);
    }
}