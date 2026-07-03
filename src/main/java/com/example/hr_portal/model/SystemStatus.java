package com.example.hr_portal.model;

public class SystemStatus {
    private String service;
    private String status;
    private String details;

    public SystemStatus(String service, String status, String details) {
        this.service = service;
        this.status = status;
        this.details = details;
    }

    public String getService() {
        return service;
    }

    public String getStatus() {
        return status;
    }

    public String getDetails() {
        return details;
    }
}