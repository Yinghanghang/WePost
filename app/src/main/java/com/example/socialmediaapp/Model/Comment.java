package com.example.socialmediaapp.Model;

public class Comment {
    private String comment;
    private String publisher;
    private String commentid;
    private long commentTime;

    public Comment(String comment, String publisher, String commentid, long commentTime) {
        this.comment = comment;
        this.publisher = publisher;
        this.commentid = commentid;
        this.commentTime = commentTime;
    }

    public Comment() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
    }

    public long getCommentTime() { return commentTime; }

    public void setCommentTime(long commentTime) { this.commentTime = commentTime; }
}