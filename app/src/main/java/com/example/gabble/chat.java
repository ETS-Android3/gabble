package com.example.gabble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gabble.sendOtp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class chat extends AppCompatActivity {

    String phoneNo;
    FirebaseFirestore db;
    DocumentReference documentReference;
    ListView listView;
    LinearLayout emptyChat;
    List<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sendOtp obj = new sendOtp();
        phoneNo = obj.getMobileNo();
        listView = findViewById(R.id.myList);
        emptyChat = findViewById(R.id.empty_chat);

        db = FirebaseFirestore.getInstance();
        loadChats();
        setChatListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.logout:
                logoutUser();
                break;
            case R.id.action_profile:
                updateProfile();
                break;
        }
        return true;
    }

    public void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(chat.this,sendOtp.class));
    }

    public void updateProfile(){
        Intent i = new Intent(getApplicationContext(),profile.class);
        startActivity(i);
    }

    public void loadChats() {
        db.collection("users").document(phoneNo).collection("chats").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list = new ArrayList<String>();
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                list.add(document.getId());
                            }
                            if(list.size()==0){
                                emptyChat.setVisibility(View.VISIBLE);
                            }
                            else {
                                emptyChat.setVisibility(View.INVISIBLE);
                                ArrayAdapter arrayAdapter =
                                        new ArrayAdapter<String>(getApplicationContext(),
                                                R.layout.activity_listview, list);
                                listView.setAdapter(arrayAdapter);
                            }
                        }
                        else{
                            Log.d("data", "error "+task.getException().getMessage());
                        }
                    }
                });
    }

    public void setChatListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                db.collection("users").document(phoneNo).collection("chats").document(list.get(position)).collection("messages").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                list = new ArrayList<String>();
                                    if(task.isSuccessful()) {

                                    }
                            }
                        });
            }
        });
    }


}