package com.example.gabble.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gabble.R;
import com.example.gabble.adapters.RecentConversationsAdapter;
import com.example.gabble.listeners.ConversationListener;
import com.example.gabble.models.ChatMessage;
import com.example.gabble.models.User;
import com.example.gabble.utilities.Constants;
import com.example.gabble.utilities.ImageConverter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends BaseActivity implements ConversationListener {

    private FloatingActionButton fab;
    private RoundedImageView imageProfile;
    private DrawerLayout drawerLayout;
    private ProgressBar progressBar;
    private NavigationView navigationView;
    private ImageView emptyChatImageView;
    private TextView emptyChatTextView;
    private RecyclerView conversationsRecyclerView;
    private SharedPreferences sharedPreferences;
    private AppCompatImageView imageStoryActivity;
    private AppCompatImageView deleteChat;
    private AppCompatImageView archiveChat;
    private ItemTouchHelper itemTouchHelper;
    private ChatMessage archivedChat = null;
    private ChatMessage deletedChat = null;

    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;
    private String name, mobile, encodedImage;
    private int position;

    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        emptyChatImageView = findViewById(R.id.empty_chat_image);
        emptyChatTextView = findViewById(R.id.empty_chat_message);
        imageStoryActivity = findViewById(R.id.imageStoryActivity);
        deleteChat = findViewById(R.id.deleteChat);
        archiveChat = findViewById(R.id.archiveChat);
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        init();
        getSharedValues();
        setListeners();
        getProfileImage();
        listenConversations();
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
            produceVibration();

            switch (direction) {
                case ItemTouchHelper.LEFT:
                case ItemTouchHelper.RIGHT:
                    archivedChat = conversations.get(position);
                    conversations.remove(position);
                    conversationsAdapter.notifyItemRemoved(position);
                    String archivedChatNo;
                    if(archivedChat.receiverNo.equals(mobile)) {
                        updateArchivedList(archivedChat.senderNo);
                        archivedChatNo = archivedChat.senderNo;
                    } else {
                        updateArchivedList(archivedChat.receiverNo);
                        archivedChatNo = archivedChat.receiverNo;
                    }
                    showUndoArchivedSnackBar(archivedChatNo);
                    break;
            }
        }

        private void showUndoDeletedSnackBar() {
            Snackbar.make(conversationsRecyclerView,
                    R.string.conversation_deleted,
                    Snackbar.LENGTH_LONG)
                    .setAnchorView(fab)
                    .setAction(R.string.snackbar_label_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            deletedChat.(archivedMovies.lastIndexOf(movieName));
                            conversations.add(position, deletedChat);
                            conversationsAdapter.notifyItemInserted(position);
                        }
                    }).show();
        }

        private void showUndoArchivedSnackBar(String mobile) {
            Snackbar.make(conversationsRecyclerView,
                    R.string.conversation_archived,
                    Snackbar.LENGTH_LONG)
                    .setAnchorView(fab)
                    .setAction(R.string.snackbar_label_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                            Set<String> archivedChats =
                                    sharedPreferences.getStringSet(Constants.KEY_ARCHIVED_CHATS,
                                            new HashSet<String>());
                            if(archivedChats.contains(mobile)) {
                                archivedChats.remove(mobile);
                                myEdit.remove(Constants.KEY_ARCHIVED_CHATS);
                                myEdit.apply();
                                myEdit.putStringSet(Constants.KEY_ARCHIVED_CHATS,archivedChats);
                                myEdit.apply();
                            }
                            conversations.add(position, archivedChat);
                            conversationsAdapter.notifyItemInserted(position);
                        }
                    }).show();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this,
                            R.color.compGreen))
                    .addSwipeLeftActionIcon(R.drawable.ic_archive)
                    .addSwipeLeftLabel(getString(R.string.swipe_archive))
                    .setSwipeLeftLabelColor(Color.WHITE)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(MainActivity.this,
                            R.color.compGreen))
                    .addSwipeRightLabel(getString(R.string.swipe_archive))
                    .addSwipeRightActionIcon(R.drawable.ic_archive)
                    .setSwipeRightLabelColor(Color.WHITE)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };

    private void produceVibration() {
        final VibrationEffect vibrationEffect1;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            vibrationEffect1 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
            vibrator.cancel();
            vibrator.vibrate(vibrationEffect1);
        }
    }

    private void updateArchivedList(String mobile) {
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        Set<String> archivedChatNumbers =
                sharedPreferences.getStringSet(Constants.KEY_ARCHIVED_CHATS,new HashSet<String>());

        archivedChatNumbers.add(mobile);
        myEdit.remove(Constants.KEY_ARCHIVED_CHATS);
        myEdit.apply();
        myEdit.putStringSet(Constants.KEY_ARCHIVED_CHATS,archivedChatNumbers);
        myEdit.apply();

        // debugging
        for(String i : archivedChatNumbers) {
            Log.d(Constants.TAG, "updateArchivedList: "+i);
        }
    }

    private void init() {
        conversations = new ArrayList<>();
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        Set<String> archivedChats = sharedPreferences.getStringSet(Constants.KEY_ARCHIVED_CHATS,
                new HashSet<String>());
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        conversationsRecyclerView = findViewById(R.id.conversationsRecyclerView);
        conversationsRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
        itemTouchHelper.attachToRecyclerView(conversationsRecyclerView);
    }

    private void getProfileImage() {
        if (encodedImage != null) {
            Log.d("demo", "getProfileImage: success");
//            imageProfile.setImageBitmap(decodeImage(encodedImage));
            new ImageConverter().loadEncodedImage(getApplicationContext(),encodedImage,imageProfile);
        }
    }

    private Bitmap decodeImage(String sImage) {
        byte[] bytes = Base64.decode(sImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
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
                    // means the chat has been archived
                } else {
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
                        chatMessage.messageType =
                                documentChange.getDocument().getString(Constants.KEY_MESSAGE_TYPE);
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
                                conversations.get(i).messageType =
                                        documentChange.getDocument().getString(Constants.KEY_MESSAGE_TYPE);
                                break;
                            }
                        }
                    }
                    //
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            conversationsRecyclerView.smoothScrollToPosition(0);
            conversationsRecyclerView.setVisibility(View.VISIBLE);
            emptyChatImageView.setVisibility(View.INVISIBLE);
            emptyChatTextView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.GONE);

            if(conversations.size()==0) {
                conversationsRecyclerView.setVisibility(View.GONE);
                emptyChatImageView.setVisibility(View.VISIBLE);
                emptyChatTextView.setVisibility(View.VISIBLE);
            }
        }
    };

    private void setListeners() {
        // setting drawer layout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                updateNavHeader();
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // For User Activity
        fab = findViewById(R.id.fabNewChat);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), UserActivity.class));
        });
        // Easter egg-1
        fab.setOnLongClickListener(v -> {
            Toast.makeText(getApplicationContext(), "\uD83D\uDC31", Toast.LENGTH_SHORT).show();
            return true;
        });

        // for opening drawer
        imageProfile = findViewById(R.id.imageProfile);
        imageProfile.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // for items in the navigation drawer
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                    i.putExtra(Constants.COMING_FROM_WHICH_ACTIVITY, Constants.MAIN_ACTIVITY);
                    startActivity(i);
                } else if (id == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), SendOtp.class));
                } else if (id == R.id.nav_archive) {
                    startActivity(new Intent(getApplicationContext(), ArchivedChatActivity.class));
                }

                return false;
            }
        });

        // for story activity
        imageStoryActivity.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), StoryActivity.class));
        });
    }

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

    private void updateNavHeader() {
        RoundedImageView nav_profile_image = findViewById(R.id.nav_profile_image);
//        nav_profile_image.setImageBitmap(decodeImage(encodedImage));
        new ImageConverter().loadEncodedImage(getApplicationContext(),encodedImage,nav_profile_image);

        TextView nav_profile_name = findViewById(R.id.nav_profile_name);
        nav_profile_name.setText(name);

        TextView nav_profile_mobile = findViewById(R.id.nav_profile_mobile);
        nav_profile_mobile.setText(mobile);
    }
}

