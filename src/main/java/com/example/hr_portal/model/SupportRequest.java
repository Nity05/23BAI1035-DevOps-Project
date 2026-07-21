package com.example.hr_portal.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SupportRequest {
    private String id;
    private String name;
    private String email;
    private String category;
    private String priority;
    private String status;
    private String message;
    private String resolutionNotes;
    private LocalDateTime submittedAt;
    private List<MetricDetail> metricsSnapshot = List.of();

    public SupportRequest() {
        this.status = "Open";
        this.priority = "P2 - Normal";
    }

    public SupportRequest(String name, String email, String category, String message) {
        this.name = name;
        this.email = email;
        this.category = category;
        this.message = message;
        this.status = "Open";
        this.priority = "P2 - Normal";
    }

    public SupportRequest(String id, String name, String email, String category, String priority, String status, String message, LocalDateTime submittedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.category = category;
        this.priority = priority != null ? priority : "P2 - Normal";
        this.status = status != null ? status : "Open";
        this.message = message;
        this.submittedAt = submittedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getFormattedSubmittedAt() {
        if (submittedAt == null) return "Just now";
        return submittedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }

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
