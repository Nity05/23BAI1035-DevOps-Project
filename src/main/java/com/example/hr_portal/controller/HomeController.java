package com.example.hr_portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.hr_portal.service.PortalService;

@Controller
public class HomeController {

    private final PortalService portalService;

    public HomeController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("news", portalService.getNews());
        model.addAttribute("announcements", portalService.getAnnouncements());
        model.addAttribute("statuses", portalService.getSystemStatuses());
        return "index";
    }

    @GetMapping("/news")
    public String news(Model model) {
        model.addAttribute("news", portalService.getNews());
        return "news";
    }

    @GetMapping("/policies")
    public String policies(Model model) {
        model.addAttribute("policies", portalService.getPolicies());
        return "policies";
    }

    @GetMapping("/holidays")
    public String holidays(Model model) {
        model.addAttribute("holidays", portalService.getHolidays());
        return "holidays";
    }

    @GetMapping("/announcements")
    public String announcements(Model model) {
        model.addAttribute("announcements", portalService.getAnnouncements());
        return "announcements";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}