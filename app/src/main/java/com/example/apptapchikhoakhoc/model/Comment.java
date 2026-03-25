package com.example.apptapchikhoakhoc.model;

public class Comment {
    private int     id;
    private int     articleId;
    private String  userName;
    private String  userEmail;
    private String  content;
    private long    timestamp;
    private String  articleTitle;
    private boolean violated;

    // Constructor cũ — giữ nguyên không break code cũ
    public Comment(int id, int articleId, String userName, String userEmail,
                   String content, long timestamp) {
        this.id           = id;
        this.articleId    = articleId;
        this.userName     = userName;
        this.userEmail    = userEmail;
        this.content      = content;
        this.timestamp    = timestamp;
        this.articleTitle = "";
        this.violated     = false;
    }

    // Constructor mới cho admin
    public Comment(int id, int articleId, String userName, String userEmail,
                   String content, long timestamp,
                   String articleTitle, boolean violated) {
        this(id, articleId, userName, userEmail, content, timestamp);
        this.articleTitle = articleTitle != null ? articleTitle : "";
        this.violated     = violated;
    }

    public int     getId()           { return id; }
    public int     getArticleId()    { return articleId; }
    public String  getUserName()     { return userName     != null ? userName     : "Ẩn danh"; }
    public String  getUserEmail()    { return userEmail    != null ? userEmail    : ""; }
    public String  getContent()      { return content      != null ? content      : ""; }
    public long    getTimestamp()    { return timestamp; }
    public String  getArticleTitle() { return articleTitle != null ? articleTitle : ""; }
    public boolean isViolated()      { return violated; }

    public void setArticleTitle(String articleTitle) { this.articleTitle = articleTitle; }
    public void setViolated(boolean violated)         { this.violated     = violated; }
}