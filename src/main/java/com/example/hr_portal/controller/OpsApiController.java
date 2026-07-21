package com.example.hr_portal.controller;

import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.hr_portal.service.PortalService;
import com.example.hr_portal.config.MetricTrackingFilter;
import com.example.hr_portal.model.SupportRequest;
import com.example.hr_portal.model.ServiceStatusInfo;

@RestController
@RequestMapping("/api/ops")
public class OpsApiController {

    private final PortalService portalService;

    public OpsApiController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getOpsSummary() {
        Map<String, Object> response = new LinkedHashMap<>();
        
        List<ServiceStatusInfo> services = portalService.getServices();
        long upServices = services.stream().filter(s -> "UP".equalsIgnoreCase(s.getStatus())).count();
        long downServices = services.stream().filter(s -> "DOWN".equalsIgnoreCase(s.getStatus())).count();

        long openTickets = portalService.getOpenSupportRequestsCount();
        long resolvedTickets = portalService.getResolvedSupportRequestsCount();
        long totalTickets = portalService.getSupportRequests().size();

        long totalRequests = MetricTrackingFilter.getTotalRequests();
        long avgLatency = MetricTrackingFilter.getAverageResponseTime();

        // Calculate simulated error rate based on down services vs total services
        double errorRate = downServices > 0 ? (double) downServices / services.size() * 100.0 : 0.0;
        String formattedErrorRate = String.format(Locale.US, "%.1f%%", errorRate);

        response.put("status", "OK");
        response.put("timestamp", System.currentTimeMillis());
        response.put("appName", "hr_portal_reliability_platform");
        response.put("version", "v1.0.0");
        response.put("totalRequests", totalRequests);
        response.put("avgResponseTimeMs", avgLatency);
        response.put("errorRate", formattedErrorRate);
        response.put("activeServicesCount", upServices);
        response.put("servicesDownCount", downServices);
        response.put("totalSupportRequests", totalTickets);
        response.put("openSupportRequests", openTickets);
        response.put("resolvedSupportRequests", resolvedTickets);
        response.put("activeIncidentsCount", portalService.getIncidents().stream().filter(i -> "Active".equalsIgnoreCase(i.getStatus())).count());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics/chart")
    public ResponseEntity<Map<String, Object>> getChartMetrics() {
        Map<String, Object> data = new LinkedHashMap<>();
        
        // Return simulated 10-point telemetry timeline for live Chart.js rendering
        List<String> timestamps = new ArrayList<>();
        List<Integer> cpuSeries = new ArrayList<>();
        List<Integer> memorySeries = new ArrayList<>();
        List<Long> latencySeries = new ArrayList<>();
        List<Long> requestSeries = new ArrayList<>();
        List<Double> errorSeries = new ArrayList<>();

        long now = System.currentTimeMillis();
        long baseLatency = MetricTrackingFilter.getAverageResponseTime();
        long baseRequests = MetricTrackingFilter.getTotalRequests();

        Random rng = new Random(42);

        for (int i = 9; i >= 0; i--) {
            long t = now - (i * 30000L); // every 30 seconds
            java.time.LocalTime lt = java.time.Instant.ofEpochMilli(t)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalTime();
            timestamps.add(String.format("%02d:%02d:%02d", lt.getHour(), lt.getMinute(), lt.getSecond()));

            cpuSeries.add(15 + rng.nextInt(25));
            memorySeries.add(40 + rng.nextInt(15));
            latencySeries.add(Math.max(12, baseLatency + rng.nextInt(30) - 10));
            requestSeries.add(Math.max(1, baseRequests + (10 - i) * 3 + rng.nextInt(5)));
            errorSeries.add(rng.nextDouble() < 0.2 ? 1.5 : 0.0);
        }

        data.put("labels", timestamps);
        data.put("cpu", cpuSeries);
        data.put("memory", memorySeries);
        data.put("latencyMs", latencySeries);
        data.put("requests", requestSeries);
        data.put("errorRate", errorSeries);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/support-requests/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable("id") String id,
            @RequestBody Map<String, String> payload) {
        
        String newStatus = payload.get("status");
        String notes = payload.get("resolutionNotes");

        if (newStatus == null || !Set.of("Open", "In Progress", "Resolved").contains(newStatus)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status must be Open, In Progress, or Resolved"));
        }

        boolean updated = portalService.updateSupportRequestStatus(id, newStatus, notes);
        if (updated) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Status updated to " + newStatus,
                "id", id,
                "status", newStatus
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
