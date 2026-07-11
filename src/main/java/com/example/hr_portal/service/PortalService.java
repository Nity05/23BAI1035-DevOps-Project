package com.example.hr_portal.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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

    public void saveSupportRequest(SupportRequest request) {
        supportRequests.add(request);
    }

    public List<SupportRequest> getSupportRequests() {
        return List.copyOf(supportRequests);
    }

    private ServiceStatusInfo checkService(String serviceName, String host, int port, String defaultUrl) {
        long start = System.currentTimeMillis();
        boolean up = false;
        try {
            // Check if socket is open with short timeout
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 600); // 600ms timeout
                up = true;
            }
        } catch (Exception e) {
            // Fallback to checking public URL if socket failed
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
        // Employee Portal is the app itself
        long selfLatency = MetricTrackingFilter.getAverageResponseTime();
        ServiceStatusInfo selfStatus = new ServiceStatusInfo("Employee Portal", "UP", selfLatency + " ms", "1 min ago");

        // Real-time checks
        ServiceStatusInfo jenkinsStatus = checkService("Jenkins", "localhost", 8080, null);
        ServiceStatusInfo gitStatus = checkService("Git Repository", "github.com", 443, "https://github.com");
        
        // VPN Gateway (designed to fail/timeout)
        ServiceStatusInfo vpnStatus = checkService("VPN Gateway", "10.255.255.1", 443, null);
        
        // Email Service
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
        // 1. CPU Load Calculation
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

        // 2. Memory Utilization
        int memVal = 45;
        try {
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            long used = total - free;
            memVal = (int) (((double) used / total) * 100);
        } catch (Exception e) {
            // Ignore
        }

        // 3. Request Rate (Requests per minute based on application uptime)
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        double minutes = uptimeMs / 60000.0;
        long totalRequests = MetricTrackingFilter.getTotalRequests();
        int rate = (minutes < 0.1) ? (int) totalRequests : (int) (totalRequests / minutes);
        if (rate == 0) {
            rate = (int) totalRequests + 2; // base simulated traffic activity if fresh start
        }

        // 4. Response Time from tracking filter
        long avgResponseTime = MetricTrackingFilter.getAverageResponseTime();

        // 5. Pod replicas count (detecting hostname)
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
