package com.example.hr_portal.model;

public class MetricDetail {
    private String name;
    private String value;
    private String icon;
    private String status;

    public MetricDetail(String name, String value, String icon, String status) {
        this.name = name;
        this.value = value;
        this.icon = icon;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getIcon() {
        return icon;
    }

    public String getStatus() {
        return status;
    }
}
