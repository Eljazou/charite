package com.example.charite.controller;

import com.example.charite.service.PendingChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final PendingChangeService pendingChangeService;

    @GetMapping("/pending")
    public String pendingChanges(Model model) {
        model.addAttribute("changes", pendingChangeService.findAllPending());
        return "admin/pending";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        pendingChangeService.approve(id);
        return "redirect:/admin/pending";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id) {
        pendingChangeService.reject(id);
        return "redirect:/admin/pending";
    }
}