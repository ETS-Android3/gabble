package com.example.gabble.listeners;

import com.example.gabble.models.User;

public interface UserListener {
    void onUserClicked(User user);
}

// think of interface like a standard that is defined for the classes that will implement it.