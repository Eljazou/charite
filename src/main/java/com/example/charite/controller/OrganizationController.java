package com.example.charite.controller;

import com.example.charite.dto.OrganizationCreateRequest;
import com.example.charite.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("organizations", organizationService.findAll());
        return "organizations/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("req", new OrganizationCreateRequest());
        return "organizations/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("req") OrganizationCreateRequest req,
                         Principal principal,
                         Model model) {
        try {
            organizationService.create(req, principal.getName());
            return "redirect:/organizations?created";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "organizations/create";
        }
    }
}