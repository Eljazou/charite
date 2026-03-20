package com.example.charite.controller;

import com.example.charite.dto.OrganizationCreateRequest;
import com.example.charite.dto.OrganizationUpdateRequest;
import com.example.charite.entity.Organization;
import com.example.charite.service.OrganizationService;
import com.example.charite.service.PendingChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;

import java.security.Principal;

@Controller
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final PendingChangeService pendingChangeService;

    @GetMapping
    public String list(Model model, Principal principal) {
        model.addAttribute("organizations", organizationService.findByUser(principal.getName()));
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
            return "redirect:/organizations?createRequested";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "organizations/create";
        }
    }
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Organization org = organizationService.findById(id);
        OrganizationUpdateRequest req = new OrganizationUpdateRequest();
        req.setName(org.getName());
        req.setLegalAddress(org.getLegalAddress());
        req.setTaxNumber(org.getTaxNumber());
        req.setMainContact(org.getMainContact());
        req.setLogoUrl(org.getLogoUrl());
        req.setDescription(org.getDescription());
        model.addAttribute("req", req);
        model.addAttribute("orgId", id);
        return "organizations/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @ModelAttribute("req") OrganizationUpdateRequest req,
                       @RequestParam("logoFile") MultipartFile logoFile,
                       Principal principal,
                       Model model) {
        try {
            if (!logoFile.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + logoFile.getOriginalFilename();
                Path uploadPath = Path.of("uploads/logos");
                Files.createDirectories(uploadPath);
                Files.copy(logoFile.getInputStream(), uploadPath.resolve(fileName));
                req.setLogoUrl("/logos/" + fileName);
            }
            pendingChangeService.requestUpdate(id, req, principal.getName());
            return "redirect:/organizations?updateRequested";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "organizations/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Principal principal) {
        pendingChangeService.requestDelete(id, principal.getName());
        return "redirect:/organizations?deleteRequested";
    }
}