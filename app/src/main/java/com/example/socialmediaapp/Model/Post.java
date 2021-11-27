package com.example.socialmediaapp.Model;

import java.util.Date;

public class Post {
    private String postID;
    private String postImage;
    private String postCaption;
    private String postAuthor;
    private String postLocation;
    private long postTime;

    public Post(String postID, String postImage, String postCaption, String postAuthor, String postLocation, long postTime) {
        this.postID = postID;
        this.postImage = postImage;
        this.postCaption = postCaption;
        this.postAuthor = postAuthor;
        this.postLocation = postLocation;
        this.postTime = postTime;
    }

    public Post() {
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostCaption() {
        return postCaption;
    }

    public void setPostCaption(String postCaption) {
        this.postCaption = postCaption;
    }

    public String getPostAuthor() {
        return postAuthor;
    }

    public void setPostAuthor(String postAuthor) {
        this.postAuthor = postAuthor;
    }

    public String getPostLocation() {
        return postLocation;
    }

    public void setPostLocation(String postLocation) {
        this.postLocation = postLocation;
    }

    public long getPostTime() { return postTime; }

    public void setPostTime(long postTime) { this.postTime = postTime; }
}
