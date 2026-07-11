package com.example.hr_portal.model;

public class ServiceStatusInfo {
    private String service;
    private String status;
    private String responseTime;
    private String lastChecked;

    public ServiceStatusInfo(String service, String status, String responseTime, String lastChecked) {
        this.service = service;
        this.status = status;
        this.responseTime = responseTime;
        this.lastChecked = lastChecked;
    }

    public String getService() {
        return service;
    }

    public String getStatus() {
        return status;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public String getLastChecked() {
        return lastChecked;
    }
}
