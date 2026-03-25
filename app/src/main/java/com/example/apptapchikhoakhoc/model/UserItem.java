package com.example.apptapchikhoakhoc.model;

public class UserItem {
    private final int    id;
    private final String name;
    private final String email;
    private final int    totalComments;
    private final int    totalLikes;

    public UserItem(int id, String name, String email,
                    int totalComments, int totalLikes) {
        this.id            = id;
        this.name          = name  != null ? name  : "Ẩn danh";
        this.email         = email != null ? email : "";
        this.totalComments = totalComments;
        this.totalLikes    = totalLikes;
    }

    public int    getId()            { return id; }
    public String getName()          { return name; }
    public String getEmail()         { return email; }
    public int    getTotalComments() { return totalComments; }
    public int    getTotalLikes()    { return totalLikes; }
}