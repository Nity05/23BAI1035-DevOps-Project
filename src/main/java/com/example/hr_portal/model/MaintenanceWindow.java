package com.example.hr_portal.model;

public class MaintenanceWindow {
    private String service;
    private String dateTime;
    private String status;

    public MaintenanceWindow(String service, String dateTime, String status) {
        this.service = service;
        this.dateTime = dateTime;
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getStatus() {
        return status;
    }
}
