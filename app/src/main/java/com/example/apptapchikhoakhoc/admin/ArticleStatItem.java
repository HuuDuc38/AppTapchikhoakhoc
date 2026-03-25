package com.example.apptapchikhoakhoc.admin;

public class ArticleStatItem {
    private final String title;
    private final int    count;
    private final String category;

    public ArticleStatItem(String title, int count, String category) {
        this.title    = title;
        this.count    = count;
        this.category = category != null ? category : "";
    }

    public ArticleStatItem(String title, int count) {
        this(title, count, "");
    }

    public String getTitle()    { return title != null ? title : ""; }
    public int    getCount()    { return count; }
    public String getCategory() { return category; }
}