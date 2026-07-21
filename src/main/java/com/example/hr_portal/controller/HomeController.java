package com.example.hr_portal.controller;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.example.hr_portal.model.MetricDetail;
import com.example.hr_portal.model.SupportRequest;
import com.example.hr_portal.service.PortalService;
import com.example.hr_portal.config.MetricTrackingFilter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Controller
public class HomeController {

    private final PortalService portalService;
    private final MeterRegistry meterRegistry;

    public HomeController(PortalService portalService, MeterRegistry meterRegistry) {
        this.portalService = portalService;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/")
    public String home(Model model) {
        var services  = portalService.getServices();
        var incidents = portalService.getIncidents();
        var maintenance = portalService.getMaintenanceWindows();
        var metrics   = portalService.getMetrics();

        long activeServices = services.stream().filter(s -> "UP".equalsIgnoreCase(s.getStatus())).count();
        long servicesDown   = services.stream().filter(s -> "DOWN".equalsIgnoreCase(s.getStatus())).count();
        long activeAlerts   = incidents.stream().filter(i -> "Active".equalsIgnoreCase(i.getStatus())).count();

        model.addAttribute("services", services);
        model.addAttribute("incidents", incidents);
        model.addAttribute("maintenanceWindows", maintenance);
        model.addAttribute("activeServicesCount", activeServices);
        model.addAttribute("servicesDownCount", servicesDown);
        model.addAttribute("ongoingMaintenanceCount", 0);
        model.addAttribute("activeAlertsCount", activeAlerts);
        model.addAttribute("deploymentVersion", "v1.0.0");

        String cpuVal      = findMetric(metrics, "CPU Usage", "12%");
        String memVal      = findMetric(metrics, "Memory Usage", "45%");
        String reqRate     = findMetric(metrics, "Request Rate", "2 / min");
        String availability = findMetric(metrics, "System Availability", "99.95%");

        model.addAttribute("cpuValue", cpuVal);
        model.addAttribute("memValue", memVal);
        model.addAttribute("requestRateValue", reqRate);
        model.addAttribute("availabilityValue", availability);

        return "index";
    }

    @GetMapping("/services")
    public String services(Model model) {
        model.addAttribute("services", portalService.getServices());
        return "services";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var services = portalService.getServices();
        var metrics = portalService.getMetrics();
        var supportRequests = portalService.getSupportRequests();
        var incidents = portalService.getIncidents();

        long upServices = services.stream().filter(s -> "UP".equalsIgnoreCase(s.getStatus())).count();
        long downServices = services.stream().filter(s -> "DOWN".equalsIgnoreCase(s.getStatus())).count();
        long openTickets = portalService.getOpenSupportRequestsCount();
        long resolvedTickets = portalService.getResolvedSupportRequestsCount();

        double errorRate = downServices > 0 ? ((double) downServices / services.size()) * 100.0 : 0.0;
        String formattedErrorRate = String.format(Locale.US, "%.1f%%", errorRate);

        model.addAttribute("metrics", metrics);
        model.addAttribute("services", services);
        model.addAttribute("supportRequests", supportRequests);
        model.addAttribute("incidents", incidents);
        model.addAttribute("upServicesCount", upServices);
        model.addAttribute("downServicesCount", downServices);
        model.addAttribute("openTicketsCount", openTickets);
        model.addAttribute("resolvedTicketsCount", resolvedTickets);
        model.addAttribute("errorRate", formattedErrorRate);
        model.addAttribute("avgLatency", MetricTrackingFilter.getAverageResponseTime() + " ms");
        model.addAttribute("totalRequests", MetricTrackingFilter.getTotalRequests());

        return "dashboard";
    }

    @GetMapping("/support-requests")
    public String supportRequests(Model model) {
        model.addAttribute("supportRequests", portalService.getSupportRequests());
        model.addAttribute("openCount", portalService.getOpenSupportRequestsCount());
        model.addAttribute("resolvedCount", portalService.getResolvedSupportRequestsCount());
        return "support-requests";
    }

    @GetMapping("/support-requests/{id}")
    public String supportRequestDetail(@PathVariable("id") String id, Model model) {
        SupportRequest supportRequest = portalService.getSupportRequestById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support request not found"));
        model.addAttribute("supportRequest", supportRequest);
        return "support-request-detail";
    }

    @PostMapping("/support-requests/{id}/status")
    public String updateSupportRequestStatus(
            @PathVariable("id") String id,
            @RequestParam("status") String status,
            @RequestParam(value = "notes", required = false) String notes,
            RedirectAttributes redirectAttributes) {

        if (!java.util.Set.of("Open", "In Progress", "Resolved").contains(status)) {
            redirectAttributes.addFlashAttribute("ticketError", "Invalid support request status.");
            return "redirect:/support-requests/" + id;
        }

        if (!portalService.updateSupportRequestStatus(id, status, notes)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Support request not found");
        }
        redirectAttributes.addFlashAttribute("ticketSuccess", "Status of ticket " + id + " updated to " + status);
        return "redirect:/support-requests/" + id;
    }

    @GetMapping("/workplace")
    public String workplace() {
        return "workplace";
    }

    @GetMapping("/maintenance")
    public String maintenance(Model model) {
        model.addAttribute("maintenanceWindows", portalService.getMaintenanceWindows());
        return "maintenance";
    }

    @GetMapping("/incidents")
    public String incidents(Model model) {
        model.addAttribute("incidents", portalService.getIncidents());
        return "incidents";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        if (!model.containsAttribute("supportRequest")) {
            model.addAttribute("supportRequest", new SupportRequest());
        }
        return "contact";
    }

    @PostMapping("/contact")
    public String submitSupportRequest(
            @ModelAttribute("supportRequest") SupportRequest request,
            RedirectAttributes redirectAttributes) {

        if (!request.isValid()) {
            redirectAttributes.addFlashAttribute("formError", "Please fill in all required fields.");
            redirectAttributes.addFlashAttribute("supportRequest", request);
            return "redirect:/contact";
        }

        String category = (request.getCategory() == null || request.getCategory().isBlank())
                ? "general" : request.getCategory();
        var metricsAtSubmission = portalService.getMetrics();

        request.setCategory(category);
        if (request.getPriority() == null || request.getPriority().isBlank()) {
            request.setPriority("incident".equals(category) ? "P1 - Critical" : "P2 - Normal");
        }
        request.setSubmittedAt(LocalDateTime.now());
        request.setMetricsSnapshot(metricsAtSubmission);
        
        portalService.saveSupportRequest(request);

        Counter.builder("support.requests.submitted")
                .description("Number of support requests submitted via the contact form")
                .tag("category", category)
                .register(meterRegistry)
                .increment();

        String metricsSummary = metricsAtSubmission.stream()
                .map(metric -> metric.getName() + "=" + metric.getValue() + " (" + metric.getStatus() + ")")
                .collect(Collectors.joining(", "));
        System.out.printf("[SupportRequest] ID=%s | submittedAt=%s | name=%s | email=%s | category=%s | message=%s | metrics=[%s]%n",
                request.getId(), request.getSubmittedAt(), request.getName(), request.getEmail(), category, request.getMessage(), metricsSummary);

        redirectAttributes.addFlashAttribute("formSuccess",
                "✅ Ticket " + request.getId() + " created successfully! The " + category + " team will respond shortly.");
        return "redirect:/contact";
    }

    private String findMetric(java.util.List<MetricDetail> metrics, String name, String fallback) {
        return metrics.stream()
                .filter(m -> name.equals(m.getName()))
                .findFirst()
                .map(MetricDetail::getValue)
                .orElse(fallback);
    }
}
