package com.example.gabble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.gabble.activities.sendOtp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class msgActivity extends AppCompatActivity {

    String receiverNo,senderNo;
    FirebaseFirestore db;
    List<String> list;
    ListView messageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg);

        sendOtp obj = new sendOtp();
        db=FirebaseFirestore.getInstance();
        messageView = findViewById(R.id.messageView);

        senderNo = obj.getMobileNo();
        receiverNo = getIntent().getStringExtra("number");
        getMessages();
    }

    public void getMessages() {
        db.collection("users").document(senderNo).collection("chats").document(receiverNo).collection("messages").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                list=new ArrayList<String>();
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        list.add(document.getId());
                    }

                    if(list.size()==0){
                        // set a graphic image for the same
                    }
                    else {
                        ArrayAdapter arrayAdapter =
                                new ArrayAdapter<String>(getApplicationContext(),
                                        R.layout.activity_listview, list);
                        messageView.setAdapter(arrayAdapter);
                    }
                }
                else {
                    Log.d("data","error "+task.getException().getMessage());
                }

            }
        });
    }
}