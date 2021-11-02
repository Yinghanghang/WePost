package com.example.socialmediaapp.Model;

public class User {
    private String userID;
    private String userName;
    private String userImage;
    private String userEmail;

    public User(String userID, String userName, String userImage, String userEmail) {
        this.userID = userID;
        this.userName = userName;
        this.userImage = userImage;
        this.userEmail = userEmail;
    }

    public User() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
