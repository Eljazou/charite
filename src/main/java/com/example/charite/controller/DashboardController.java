package com.example.charite.controller;

import com.example.charite.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("stats", dashboardService.getStats(principal.getName()));
        model.addAttribute("currentUrl", "/dashboard");
        return "dashboard/index";
    }
}