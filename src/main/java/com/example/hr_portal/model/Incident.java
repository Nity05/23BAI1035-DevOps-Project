package com.example.hr_portal.model;

public class Incident {
    private String title;
    private String description;
    private String status;
    private String timestamp;

    public Incident(String title, String description, String status, String timestamp) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
