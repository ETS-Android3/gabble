package com.example.gabble.activities;

import static android.app.PendingIntent.getActivity;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.gabble.R;
import com.example.gabble.adapters.ChatAdapter;
import com.example.gabble.databinding.ActivityChatBinding;
import com.example.gabble.databinding.ActivityUserBinding;
import com.example.gabble.models.ChatMessage;
import com.example.gabble.models.User;
import com.example.gabble.utilities.Constants;
import com.example.gabble.utilities.GetUserInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import java.io.ByteArrayOutputStream;
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
    private DocumentReference documentReference;
    private SharedPreferences sharedPreferences;
    private String senderMobileNo;
    private String senderName;
    private String senderImage;
    private String conversationId = null;
    private boolean isReceiverOnline = false;
    private boolean isReceiverTyping = false;
    private String messageType = null;

    // constants
    public static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EmojiManager.install(new GoogleEmojiProvider());

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
        listenAvailabilityOfUser();
        listenTypingOfUser();
    }

    private void init() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, senderMobileNo);
        binding.chatRecylerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(senderMobileNo);
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_MOBILE, senderMobileNo);
        message.put(Constants.KEY_RECEIVER_MOBILE, receiverUser.phoneNo);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        message.put(Constants.KEY_MESSAGE_TYPE,Constants.KEY_TYPE_TEXT);
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        messageType = Constants.KEY_TYPE_TEXT;

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
            conversation.put(Constants.KEY_MESSAGE_TYPE,Constants.KEY_TYPE_TEXT);
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
                    chatMessage.messageType =
                            documentChange.getDocument().getString(Constants.KEY_MESSAGE_TYPE);
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

        // arrangement for QR activity
        if(receiverUser==null) {
            receiverUser = new User();
            String mobile = getIntent().getStringExtra(Constants.KEY_MOBILE);
            GetUserInformation getUserInformation = new GetUserInformation(mobile);
            receiverUser.phoneNo = mobile;
            receiverUser.name = getUserInformation.getName();
            receiverUser.image = getUserInformation.getImage();
            receiverUser.about = getUserInformation.getImage();
        }

        binding.textName.setText(receiverUser.name);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        });

        binding.sendButton.setOnClickListener(v -> {
            sendMessage();
            documentReference.update(Constants.KEY_TYPING,false);
        });

        // for Emoji keyboard
        EmojiPopup popup =
                EmojiPopup.Builder.fromRootView(binding.getRoot()).build(binding.inputMessage);
        binding.layoutEmoji.setOnClickListener(v -> {
            popup.toggle();
        });

        // for enabling/disabling attach button
        binding.inputMessage.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                documentReference.update(Constants.KEY_TYPING,true);
                if(s.toString().equals("")) {
                    binding.sendButton.setVisibility(View.GONE);
                    binding.attachButton.setVisibility(View.VISIBLE);
                } else {
                    binding.sendButton.setVisibility(View.VISIBLE);
                    binding.attachButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // for attach button
        binding.attachButton.setOnClickListener(v-> {
            showAlertBox();
        });

        // for typing purpose
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                binding.getRoot().getWindowVisibleDisplayFrame(r);
                int screenHeight = binding.getRoot().getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {

                } else {
                    documentReference.update(Constants.KEY_TYPING,false);
                }
            }
        });
    }

    // For choosing images, videos, location, contacts, documents
    private void showAlertBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose File type");

        String[] types = {"Image","Video","Document","Contacts","Location"};
        builder.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        getImage();
                        break;
                    case 1:
                        break; // video
                    case 2:
                        break; // doc
                    case 3:
                        break; // contacts
                    case 4:
                        break; // location
                }

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private void sendImageMessage(String encodedImage) {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_MOBILE, senderMobileNo);
        message.put(Constants.KEY_RECEIVER_MOBILE, receiverUser.phoneNo);
        message.put(Constants.KEY_MESSAGE, encodedImage);
        message.put(Constants.KEY_MESSAGE_TYPE,Constants.KEY_TYPE_IMAGE);
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        messageType = Constants.KEY_TYPE_IMAGE;

        if (conversationId != null) {
            updateConversation(encodedImage);
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_RECEIVER_MOBILE, receiverUser.phoneNo);
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversation.put(Constants.KEY_SENDER_NAME,
                    sharedPreferences.getString(Constants.KEY_NAME, ""));
            conversation.put(Constants.KEY_SENDER_MOBILE, senderMobileNo);
            conversation.put(Constants.KEY_SENDER_IMAGE,
                    sharedPreferences.getString(Constants.KEY_IMAGE, null));
            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversation.put(Constants.KEY_LAST_MESSAGE, encodedImage);
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            conversation.put(Constants.KEY_MESSAGE_TYPE,Constants.KEY_TYPE_IMAGE);
            addConversation(conversation);
        }

        binding.inputMessage.setText(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            Uri selectedImageUri = data.getData();
            if(selectedImageUri!=null) {
                String encodedImage = convertImage(selectedImageUri);
                sendImageMessage(encodedImage);
            } else {
                // make toast
            }
        }
    }

    private String convertImage(Uri imageUri) {
        try {
            Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,stream);
            byte[] bytes=stream.toByteArray();
            return Base64.encodeToString(bytes,Base64.DEFAULT);
        } catch (Exception e) {
            Log.d("demo", "convertImage: "+e.getMessage());
        }
        return null;
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
                receiverUser.name,Constants.KEY_RECEIVER_IMAGE,receiverUser.image,
                Constants.KEY_MESSAGE_TYPE,messageType);
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

    private void listenAvailabilityOfUser() {
        Log.d(Constants.TAG, "listenAvailabilityOfUser: I am here");
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.phoneNo
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if(error!=null) {
                return;
            }
            if(value!=null) {
                if(value.getString(Constants.KEY_AVAILABILITY)!=null) {
                    Log.d(Constants.TAG, "listenAvailabilityOfUser: I am here");
                    if(value.getString(Constants.KEY_AVAILABILITY).equals(Constants.KEY_ONLINE)) {
                        isReceiverOnline = true;
                    }
                    else {
                        isReceiverOnline = false;
                    }
                }
            }
            if(isReceiverOnline) {
                Log.d(Constants.TAG, "listenAvailabilityOfUser: I am here");
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }

        });
    }

    private void listenTypingOfUser() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.phoneNo
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if(error!=null) {
                return;
            }
            if(value!=null) {
                if(value.getBoolean(Constants.KEY_TYPING)!=null) {
                    if(value.getBoolean(Constants.KEY_TYPING)) {
                        isReceiverTyping = true;
                    }
                    else {
                        isReceiverTyping = false;
                    }
                }
            }
            if(isReceiverTyping) {
                binding.textAvailability.setText("typing...");
            } else {
                binding.textAvailability.setText(R.string.online);
            }

        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }
}