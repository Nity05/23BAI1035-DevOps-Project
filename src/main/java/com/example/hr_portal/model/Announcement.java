package com.example.hr_portal.model;

public class Announcement {
    private String title;
    private String message;

    public Announcement(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}