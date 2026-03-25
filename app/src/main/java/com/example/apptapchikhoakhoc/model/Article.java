package com.example.apptapchikhoakhoc.model;

import java.io.Serializable;

public class Article implements Serializable {

    private int id;
    private String title;
    private String author;
    private String category;
    private String content;
    private String imagePath;
    private String videoPath;

    private int likes = 0;
    private int comments = 0;
    private int shares = 0;
    private int viewCount = 0; // ← MỚI
    private String status = "approved";
    private String userEmail = "";
    private long approvedAt;

    public long getApprovedAt() { return approvedAt; }
    public void setApprovedAt(long approvedAt) { this.approvedAt = approvedAt; }

    public Article(int id, String title, String author, String category,
                   String content, String imagePath, String videoPath) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.content = content;
        this.imagePath = imagePath;
        this.videoPath = videoPath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author != null ? author : "Không rõ"; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }

    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }

    // ← MỚI
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserEmail() { return userEmail != null ? userEmail : ""; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}