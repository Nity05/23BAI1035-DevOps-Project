package com.example.hr_portal.model;

import java.time.LocalDateTime;
import java.util.List;

public class SupportRequest {
    private String name;
    private String email;
    private String category;
    private String message;
    private LocalDateTime submittedAt;
    private List<MetricDetail> metricsSnapshot = List.of();

    // Default constructor needed for Spring MVC model binding
    public SupportRequest() {}

    public SupportRequest(String name, String email, String category, String message) {
        this.name = name;
        this.email = email;
        this.category = category;
        this.message = message;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public List<MetricDetail> getMetricsSnapshot() { return metricsSnapshot; }
    public void setMetricsSnapshot(List<MetricDetail> metricsSnapshot) {
        this.metricsSnapshot = metricsSnapshot == null ? List.of() : List.copyOf(metricsSnapshot);
    }

    public boolean isValid() {
        return name != null && !name.isBlank()
            && email != null && email.contains("@")
            && message != null && !message.isBlank();
    }
}
