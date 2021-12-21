package com.example.gabble.listeners;

import com.example.gabble.models.User;

public interface ConversationListener {
    void onConversationClicked(User user);
    void onConversationLongClicked(User user);
}
