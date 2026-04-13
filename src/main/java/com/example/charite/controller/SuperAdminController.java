package com.example.charite.controller;

import com.example.charite.dto.RegisterRequest;
import com.example.charite.enums.Role;
import com.example.charite.service.PendingChangeService;
import com.example.charite.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final PendingChangeService pendingChangeService;
    private final UserService userService;

    @GetMapping("/pending")
    public String pendingChanges(Model model) {
        model.addAttribute("changes", pendingChangeService.findAllPending());
        model.addAttribute("currentUrl", "/admin/pending");
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
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("currentUrl", "/admin/users");
        return "admin/users";
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("req", new RegisterRequest());
        model.addAttribute("roles", Role.values());
        return "admin/user-create";
    }

    @PostMapping("/users/create")
    public String createUser(@ModelAttribute("req") RegisterRequest req,
                             @RequestParam("role") String role,
                             Model model) {
        try {
            userService.registerWithRole(req, role);
            return "redirect:/admin/users?created";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", Role.values());
            return "admin/user-create";
        }
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        model.addAttribute("roles", Role.values());
        return "admin/user-edit";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                           @RequestParam("fullName") String fullName,
                           @RequestParam("email") String email,
                           @RequestParam("role") String role,
                           Model model) {
        try {
            userService.update(id, fullName, email, role);
            return "redirect:/admin/users?updated";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", userService.findById(id));
            model.addAttribute("roles", Role.values());
            return "admin/user-edit";
        }
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/admin/users?deleted";
    }
}