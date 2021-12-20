package com.example.gabble.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Story implements Serializable {
    public String senderName,senderNo,profileImage,fontFamilyId;
    public Date dateObject;
    public String textMessage;

    // for storing content in date and its content format
    public HashMap<Date,String> storyContent;
}
