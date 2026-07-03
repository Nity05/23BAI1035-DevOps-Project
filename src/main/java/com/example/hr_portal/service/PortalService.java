package com.example.hr_portal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.hr_portal.model.Announcement;
import com.example.hr_portal.model.Holiday;
import com.example.hr_portal.model.NewsItem;
import com.example.hr_portal.model.Policy;
import com.example.hr_portal.model.SystemStatus;

@Service
public class PortalService {

    public List<NewsItem> getNews() {
        return List.of(
                new NewsItem("Quarterly Town Hall", "All employees are invited to the company-wide town hall.", "10 July 2026"),
                new NewsItem("Security Awareness Week", "Employees must complete the security training module.", "15 July 2026"),
                new NewsItem("New Office Location", "The company has opened a new branch office.", "20 July 2026")
        );
    }

    public List<Policy> getPolicies() {
        return List.of(
                new Policy("Leave Policy", "Employees can apply for annual, sick, and emergency leave through HR.", "Active"),
                new Policy("Remote Work Policy", "Remote work is allowed with manager approval.", "Active"),
                new Policy("Code of Conduct", "Employees must follow workplace ethics and company guidelines.", "Active")
        );
    }

    public List<Holiday> getHolidays() {
        return List.of(
                new Holiday("15 August 2026", "Independence Day"),
                new Holiday("02 October 2026", "Gandhi Jayanti"),
                new Holiday("24 December 2026", "Christmas")
        );
    }

    public List<Announcement> getAnnouncements() {
        return List.of(
                new Announcement("Portal Maintenance", "The intranet portal will be updated this weekend."),
                new Announcement("HR Helpdesk", "Employees can contact HR for policy-related questions."),
                new Announcement("Performance Review", "Quarterly performance review forms are now available.")
        );
    }

    public List<SystemStatus> getSystemStatuses() {
        return List.of(
                new SystemStatus("Kubernetes Deployment", "Running", "Two portal replicas are active."),
                new SystemStatus("Nagios Monitoring", "Active", "Website uptime monitoring is enabled."),
                new SystemStatus("Graphite Metrics", "Collecting", "Infrastructure metrics are being collected."),
                new SystemStatus("Grafana Dashboard", "Available", "Administrators can view system health dashboards.")
        );
    }
}