package com.example.gabble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gabble.activities.profile;
import com.example.gabble.activities.sendOtp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sendOtp obj = new sendOtp();
        phoneNo = obj.getMobileNo();
        listView = findViewById(R.id.myList);
        emptyChat = findViewById(R.id.empty_chat);
        fab = findViewById(R.id.fab);

        setFabListener();

        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            case R.id.note:
                NoteToSelf();
                break;
        }
        return true;
    }

    public void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(chat.this,sendOtp.class));
    }

    public void updateProfile(){
        Intent i = new Intent(getApplicationContext(), profile.class);
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
                String number = list.get(position);
                db.collection("users").document(phoneNo).collection("chats").document(number).collection("messages").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                list = new ArrayList<String>();
                                    if(task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "fetched messages",
                                                Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(getApplicationContext(),
                                                msgActivity.class);
                                        i.putExtra("number",number);
                                        startActivity(i);
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "Unable to fetch " +
                                                        "messages",
                                                Toast.LENGTH_SHORT).show();
                                    }
                            }
                        });
        }
    });
    }

    public void NoteToSelf() {

    }

    public void setFabListener() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(chat.this,contacts.class);
                startActivity(i);
            }
        });
    }

}