package com.example.gabble.models;

import java.io.Serializable;

// why I used serializable?
// Was facing problem in passing object of this class in the putExtra method
public class User implements Serializable {
    public String name,phoneNo,about,image;
}
