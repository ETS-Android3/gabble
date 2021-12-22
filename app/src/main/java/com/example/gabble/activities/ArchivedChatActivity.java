package com.example.gabble.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.gabble.R;
import com.example.gabble.adapters.RecentConversationsAdapter;
import com.example.gabble.databinding.ActivityArchivedChatBinding;
import com.example.gabble.listeners.ConversationListener;
import com.example.gabble.models.ChatMessage;
import com.example.gabble.models.User;
import com.example.gabble.utilities.Constants;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ArchivedChatActivity extends BaseActivity implements ConversationListener {

    private ActivityArchivedChatBinding binding;
    private SharedPreferences sharedPreferences;
    private ItemTouchHelper itemTouchHelper;

    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;
    private String name, mobile, encodedImage;
    private int position;
    private ChatMessage unArchivedChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArchivedChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,MODE_PRIVATE);

        init();
        getSharedValues();
        setListeners();
        listenConversations();
    }

    private void init() {
        conversations = new ArrayList<>();
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        Set<String> archivedChats = sharedPreferences.getStringSet(Constants.KEY_ARCHIVED_CHATS,
                null);
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
        itemTouchHelper.attachToRecyclerView(binding.conversationsRecyclerView);
    }

    private void setListeners() {
        // For image Back
        binding.imageBack.setOnClickListener(v-> {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        });
    }

    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_MOBILE,
                        sharedPreferences.getString(Constants.KEY_MOBILE, ""))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_MOBILE,
                        sharedPreferences.getString(Constants.KEY_MOBILE, ""))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        Set<String> archivedChatNumbers =
                sharedPreferences.getStringSet(Constants.KEY_ARCHIVED_CHATS,new HashSet<String>());
        //
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if(archivedChatNumbers.contains(documentChange.getDocument().getString(Constants.KEY_RECEIVER_MOBILE))
                        || archivedChatNumbers.contains(documentChange.getDocument().getString(Constants.KEY_SENDER_MOBILE)) ) {

                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        String senderNo =
                                documentChange.getDocument().getString(Constants.KEY_SENDER_MOBILE);
                        String receiverNo =
                                documentChange.getDocument().getString(Constants.KEY_RECEIVER_MOBILE);
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderNo = senderNo;
                        chatMessage.receiverNo = receiverNo;
                        if (sharedPreferences.getString(Constants.KEY_MOBILE, "").equals(senderNo)) {
                            chatMessage.conversationId =
                                    documentChange.getDocument().getString(Constants.KEY_RECEIVER_MOBILE);
                            chatMessage.conversationName =
                                    documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                            chatMessage.conversationImage =
                                    documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        } else {
                            chatMessage.conversationName =
                                    documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                            chatMessage.conversationId =
                                    documentChange.getDocument().getString(Constants.KEY_SENDER_MOBILE);
                            chatMessage.conversationImage =
                                    documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        }
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE
                        );
                        chatMessage.dateObject =
                                documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        conversations.add(chatMessage);
                    } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                        for (int i = 0; i < conversations.size(); i++) {
                            String senderNo =
                                    documentChange.getDocument().getString(Constants.KEY_SENDER_MOBILE);
                            String receiverNo =
                                    documentChange.getDocument().getString(Constants.KEY_RECEIVER_MOBILE);
                            if (conversations.get(i).senderNo.equals(senderNo) && conversations.get(i).receiverNo.equals(receiverNo)) {
                                conversations.get(i).message =
                                        documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                conversations.get(i).dateObject =
                                        documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                                break;
                            }
                        }
                    }
                    //
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyChatImage.setVisibility(View.INVISIBLE);
            binding.emptyChatMessage.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.GONE);

            if(conversations.size()==0) {
                binding.conversationsRecyclerView.setVisibility(View.GONE);
                binding.emptyChatImage.setVisibility(View.VISIBLE);
                binding.emptyChatMessage.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    private void getSharedValues() {
        encodedImage = sharedPreferences.getString(Constants.KEY_IMAGE, null);
        name = sharedPreferences.getString(Constants.KEY_NAME, null);
        mobile = sharedPreferences.getString(Constants.KEY_MOBILE, null);
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            position = viewHolder.getAdapterPosition();

            switch (direction) {
                case ItemTouchHelper.LEFT:
                case ItemTouchHelper.RIGHT:
                    unArchivedChat = conversations.get(position);
                    conversations.remove(position);
                    conversationsAdapter.notifyItemRemoved(position);
                    String unArchivedChatNo;
                    if(unArchivedChat.receiverNo.equals(mobile)) {
                        updateUnArchivedList(unArchivedChat.senderNo);
                        unArchivedChatNo = unArchivedChat.senderNo;
                    } else {
                        updateUnArchivedList(unArchivedChat.receiverNo);
                        unArchivedChatNo = unArchivedChat.receiverNo;
                    }
                    showUndoUnArchivedSnackBar(unArchivedChatNo);
                    break;
            }
        }

        private void showUndoUnArchivedSnackBar(String mobile) {
            Snackbar.make(binding.conversationsRecyclerView,
                    R.string.conversation_unarchived,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_label_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                            Set<String> archivedChats =
                                    sharedPreferences.getStringSet(Constants.KEY_ARCHIVED_CHATS,
                                            new HashSet<String>());

                            archivedChats.add(mobile);
                            myEdit.remove(Constants.KEY_ARCHIVED_CHATS);
                            myEdit.apply();
                            myEdit.putStringSet(Constants.KEY_ARCHIVED_CHATS,archivedChats);
                            myEdit.apply();

                            conversations.add(position, unArchivedChat);
                            conversationsAdapter.notifyItemInserted(position);
                        }
                    }).show();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                            R.color.yellowTint))
                    .addSwipeLeftActionIcon(R.drawable.ic_unarchive)
                    .addSwipeLeftLabel(getString(R.string.swipe_unarchive))
                    .setSwipeLeftLabelColor(Color.WHITE)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                            R.color.yellowTint))
                    .addSwipeRightLabel(getString(R.string.swipe_unarchive))
                    .addSwipeRightActionIcon(R.drawable.ic_unarchive)
                    .setSwipeRightLabelColor(Color.WHITE)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };

    private void updateUnArchivedList(String mobile) {
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        Set<String> archivedChatNumbers =
                sharedPreferences.getStringSet(Constants.KEY_ARCHIVED_CHATS,new HashSet<String>());

        archivedChatNumbers.remove(mobile);
        myEdit.remove(Constants.KEY_ARCHIVED_CHATS);
        myEdit.apply();
        myEdit.putStringSet(Constants.KEY_ARCHIVED_CHATS,archivedChatNumbers);
        myEdit.apply();

        // debugging
        for(String i : archivedChatNumbers) {
            Log.d(Constants.TAG, "updateArchivedList: "+i);
        }
    }
}