package com.example.gabble.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.example.gabble.R;
import com.example.gabble.adapters.ChatAdapter;
import com.example.gabble.databinding.ActivityChatBinding;
import com.example.gabble.databinding.ActivityUserBinding;
import com.example.gabble.models.ChatMessage;
import com.example.gabble.models.User;
import com.example.gabble.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends BaseActivity {

    ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore database;
    private SharedPreferences sharedPreferences;
    private String senderMobileNo;
    private String senderName;
    private String senderImage;
    private String conversationId = null;
    private boolean isReceiverOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        senderMobileNo = sharedPreferences.getString(Constants.KEY_MOBILE, "");
        senderName = sharedPreferences.getString(Constants.KEY_NAME, "");
        senderImage = sharedPreferences.getString(Constants.KEY_IMAGE, "");

        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabiltyOfUser();
    }

    private void init() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, senderMobileNo);
        binding.chatRecylerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_MOBILE, senderMobileNo);
        message.put(Constants.KEY_RECEIVER_MOBILE, receiverUser.phoneNo);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);

        if (conversationId != null) {
            updateConversation(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            Log.d(Constants.TAG, "sendMessage: "+receiverUser.phoneNo);
            conversation.put(Constants.KEY_RECEIVER_MOBILE, receiverUser.phoneNo);
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversation.put(Constants.KEY_SENDER_NAME,
                    sharedPreferences.getString(Constants.KEY_NAME, ""));
            conversation.put(Constants.KEY_SENDER_MOBILE, senderMobileNo);
            conversation.put(Constants.KEY_SENDER_IMAGE,
                    sharedPreferences.getString(Constants.KEY_IMAGE, null));
            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);
        }

        binding.inputMessage.setText(null);
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_MOBILE, senderMobileNo)
                .whereEqualTo(Constants.KEY_RECEIVER_MOBILE, receiverUser.phoneNo)
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_MOBILE, receiverUser.phoneNo)
                .whereEqualTo(Constants.KEY_RECEIVER_MOBILE, senderMobileNo)
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderNo = documentChange.getDocument().getString(Constants.KEY_SENDER_MOBILE);
                    chatMessage.receiverNo =
                            documentChange.getDocument().getString(Constants.KEY_RECEIVER_MOBILE);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime =
                            getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject =
                            documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecylerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecylerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversationId == null) {
            checkForConversation();
        }
    };

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        });
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String, Object> conversation) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId =
                        documentReference.getId());
    }

    private void updateConversation(String message) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP,
                new Date(),Constants.KEY_SENDER_NAME,senderName,Constants.KEY_SENDER_MOBILE,
                senderMobileNo,Constants.KEY_SENDER_IMAGE,senderImage,
                Constants.KEY_RECEIVER_MOBILE,receiverUser.phoneNo,Constants.KEY_RECEIVER_NAME,
                receiverUser.name,Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
    }

    private void checkForConversation() {
        if (chatMessages.size() != 0) {
            checkForConversationsRemotely(
                    senderMobileNo,
                    receiverUser.phoneNo
            );
            checkForConversationsRemotely(
                    receiverUser.phoneNo,
                    senderMobileNo
            );
        }
    }

    private void checkForConversationsRemotely(String senderMobileNo, String receiverMobileNo) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_MOBILE, senderMobileNo)
                .whereEqualTo(Constants.KEY_RECEIVER_MOBILE, receiverMobileNo)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    private void listenAvailabiltyOfUser() {
        Log.d(Constants.TAG, "listenAvailabilityOfUser: I am here");
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.phoneNo
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if(error!=null) {
                return;
            }
            if(value!=null) {
                if(value.getString(Constants.KEY_AVAILABILITY)!=null) {
                    Log.d(Constants.TAG, "listenAvailabiltyOfUser: I am here");
                    if(value.getString(Constants.KEY_AVAILABILITY).equals(Constants.KEY_ONLINE)) {
                        isReceiverOnline = true;
                    }
                    else {
                        isReceiverOnline = false;
                    }
                }
            }
            if(isReceiverOnline) {
                Log.d(Constants.TAG, "listenAvailabiltyOfUser: I am here");
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }

        });
    }

}