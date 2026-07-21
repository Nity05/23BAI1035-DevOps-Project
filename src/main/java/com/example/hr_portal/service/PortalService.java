package com.example.hr_portal.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

import com.example.hr_portal.model.ServiceStatusInfo;
import com.example.hr_portal.model.MaintenanceWindow;
import com.example.hr_portal.model.Incident;
import com.example.hr_portal.model.MetricDetail;
import com.example.hr_portal.model.SupportRequest;
import com.example.hr_portal.config.MetricTrackingFilter;

@Service
public class PortalService {

    private final List<SupportRequest> supportRequests = new CopyOnWriteArrayList<>();
    private final AtomicInteger requestIdSequence = new AtomicInteger(1000);

    public PortalService() {
        seedInitialSupportRequests();
    }

    private void seedInitialSupportRequests() {
        SupportRequest r1 = new SupportRequest(
            generateNextId(),
            "Sarah Jenkins",
            "sarah.j@company.com",
            "incident",
            "P1 - Critical",
            "Open",
            "VPN authentication fails intermittently during peak hours (10 AM - 12 PM).",
            LocalDateTime.now().minusHours(3)
        );

        SupportRequest r2 = new SupportRequest(
            generateNextId(),
            "Alex Rivera",
            "arivera@company.com",
            "infra",
            "P2 - High",
            "In Progress",
            "Requesting CPU & memory allocation increase for staging Kubernetes namespace.",
            LocalDateTime.now().minusHours(18)
        );

        SupportRequest r3 = new SupportRequest(
            generateNextId(),
            "DevOps Alert System",
            "alerts@company.com",
            "incident",
            "P2 - High",
            "Open",
            "Elevated response latency observed on API Gateway endpoint (>450ms).",
            LocalDateTime.now().minusMinutes(45)
        );

        SupportRequest r4 = new SupportRequest(
            generateNextId(),
            "Elena Rostova",
            "elena.r@company.com",
            "security",
            "P3 - Low",
            "Resolved",
            "Access role provisioning for new SRE team member complete.",
            LocalDateTime.now().minusDays(1)
        );
        r4.setResolutionNotes("Granted SRE-Readonly Kubernetes role and Grafana viewer permissions.");

        supportRequests.add(r1);
        supportRequests.add(r2);
        supportRequests.add(r3);
        supportRequests.add(r4);
    }

    public String generateNextId() {
        return "REQ-" + requestIdSequence.incrementAndGet();
    }

    public void saveSupportRequest(SupportRequest request) {
        if (request.getId() == null || request.getId().isBlank()) {
            request.setId(generateNextId());
        }
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            request.setStatus("Open");
        }
        if (request.getPriority() == null || request.getPriority().isBlank()) {
            request.setPriority("P2 - Normal");
        }
        if (request.getSubmittedAt() == null) {
            request.setSubmittedAt(LocalDateTime.now());
        }
        supportRequests.add(0, request); // newest top
    }

    public List<SupportRequest> getSupportRequests() {
        return List.copyOf(supportRequests);
    }

    public Optional<SupportRequest> getSupportRequestById(String id) {
        return supportRequests.stream()
                .filter(r -> r.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public boolean updateSupportRequestStatus(String id, String newStatus, String notes) {
        Optional<SupportRequest> optReq = getSupportRequestById(id);
        if (optReq.isPresent()) {
            SupportRequest req = optReq.get();
            req.setStatus(newStatus);
            if (notes != null && !notes.isBlank()) {
                req.setResolutionNotes(notes);
            }
            return true;
        }
        return false;
    }

    public long getOpenSupportRequestsCount() {
        return supportRequests.stream()
                .filter(r -> "Open".equalsIgnoreCase(r.getStatus()) || "In Progress".equalsIgnoreCase(r.getStatus()))
                .count();
    }

    public long getResolvedSupportRequestsCount() {
        return supportRequests.stream()
                .filter(r -> "Resolved".equalsIgnoreCase(r.getStatus()))
                .count();
    }

    private ServiceStatusInfo checkService(String serviceName, String host, int port, String defaultUrl) {
        long start = System.currentTimeMillis();
        boolean up = false;
        try {
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 600);
                up = true;
            }
        } catch (Exception e) {
            if (defaultUrl != null) {
                try {
                    java.net.URL url = new java.net.URL(defaultUrl);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(600);
                    conn.setReadTimeout(600);
                    conn.setRequestMethod("GET");
                    int code = conn.getResponseCode();
                    if (code >= 200 && code < 400) {
                        up = true;
                    }
                } catch (Exception ex) {
                    up = false;
                }
            }
        }
        long duration = System.currentTimeMillis() - start;
        String status = up ? "UP" : "DOWN";
        String responseTime = up ? (duration + " ms") : "Timeout";
        String lastChecked = "30 sec ago";
        return new ServiceStatusInfo(serviceName, status, responseTime, lastChecked);
    }

    public List<ServiceStatusInfo> getServices() {
        long selfLatency = MetricTrackingFilter.getAverageResponseTime();
        ServiceStatusInfo selfStatus = new ServiceStatusInfo("Employee Portal", "UP", selfLatency + " ms", "1 min ago");

        ServiceStatusInfo jenkinsStatus = checkService("Jenkins", "localhost", 8080, null);
        ServiceStatusInfo gitStatus = checkService("Git Repository", "github.com", 443, "https://github.com");
        ServiceStatusInfo vpnStatus = checkService("VPN Gateway", "10.255.255.1", 443, null);
        ServiceStatusInfo emailStatus = checkService("Email Service", "smtp.gmail.com", 587, null);

        return List.of(selfStatus, jenkinsStatus, gitStatus, vpnStatus, emailStatus);
    }

    public List<MaintenanceWindow> getMaintenanceWindows() {
        return List.of(
                new MaintenanceWindow("Jenkins Upgrade", "12 July 2026 11 PM", "Scheduled"),
                new MaintenanceWindow("VPN Maintenance", "14 July 2026 10 PM", "Scheduled"),
                new MaintenanceWindow("Database Backup", "16 July 2026 1 AM", "Scheduled")
        );
    }

    public List<Incident> getIncidents() {
        return List.of(
                new Incident("VPN service interruption resolved.", "VPN gateway experienced a temporary timeout due to network routing changes. All connections are fully restored.", "Resolved", "1 hour ago"),
                new Incident("Elevated API latency observed.", "We are observing increased latency on API gateway endpoints. Engineers are investigating.", "Active", "30 mins ago"),
                new Incident("Email delivery delays detected.", "Delays in downstream email relay queues have been identified. Remediation in progress.", "Active", "15 mins ago")
        );
    }

    public List<MetricDetail> getMetrics() {
        int cpuVal = 12;
        try {
            var osBean = java.lang.management.ManagementFactory.getPlatformMXBean(
                com.sun.management.OperatingSystemMXBean.class
            );
            double cpuLoad = osBean.getCpuLoad();
            if (cpuLoad >= 0) {
                cpuVal = (int) (cpuLoad * 100);
            }
        } catch (Exception e) {
            // Ignore
        }

        int memVal = 45;
        try {
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            long used = total - free;
            memVal = (int) (((double) used / total) * 100);
        } catch (Exception e) {
            // Ignore
        }

        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        double minutes = uptimeMs / 60000.0;
        long totalRequests = MetricTrackingFilter.getTotalRequests();
        int rate = (minutes < 0.1) ? (int) totalRequests : (int) (totalRequests / minutes);
        if (rate == 0) {
            rate = (int) totalRequests + 2;
        }

        long avgResponseTime = MetricTrackingFilter.getAverageResponseTime();

        String envHost = System.getenv("HOSTNAME");
        int podCount = (envHost != null && envHost.startsWith("hr-portal-")) ? 2 : 1;

        return List.of(
                new MetricDetail("CPU Usage", cpuVal + "%", "💻", "Healthy"),
                new MetricDetail("Memory Usage", memVal + "%", "💾", "Healthy"),
                new MetricDetail("Request Rate", rate + " / min", "📈", "Healthy"),
                new MetricDetail("Average Response Time", avgResponseTime + " ms", "⏱️", "Healthy"),
                new MetricDetail("Pod Count", podCount + " Running", "📦", "Healthy"),
                new MetricDetail("Deployment Version", "v1.0.0", "⚙️", "Healthy"),
                new MetricDetail("System Availability", "99.95%", "🌐", "Healthy")
        );
    }
}
