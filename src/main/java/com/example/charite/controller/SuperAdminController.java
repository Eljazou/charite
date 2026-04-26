package com.example.charite.controller;

import com.example.charite.dto.PasswordRequest;
import com.example.charite.dto.ProfileRequest;
import com.example.charite.dto.RegisterRequest;
import com.example.charite.enums.Role;
import com.example.charite.service.PendingChangeService;
import com.example.charite.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final PendingChangeService pendingChangeService;
    private final UserService userService;

    @GetMapping("/pending")
    public String pendingChanges(Model model) {
        model.addAttribute("changes", pendingChangeService.findAllPending());
        model.addAttribute("currentUrl", "/superadmin/pending");
        return "superadmin/pending";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        pendingChangeService.approve(id);
        return "redirect:/superadmin/pending";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id) {
        pendingChangeService.reject(id);
        return "redirect:/superadmin/pending";
    }
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("currentUrl", "/superadmin/users");
        return "superadmin/users";
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("req", new RegisterRequest());
        model.addAttribute("roles", Role.values());
        return "superadmin/user-create";
    }

    @PostMapping("/users/create")
    public String createUser(@ModelAttribute("req") RegisterRequest req,
                             @RequestParam("role") String role,
                             Model model) {
        try {
            userService.registerWithRole(req, role);
            return "redirect:/superadmin/users?created";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", Role.values());
            return "superadmin/user-create";
        }
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        model.addAttribute("roles", Role.values());
        return "superadmin/user-edit";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                           @RequestParam("fullName") String fullName,
                           @RequestParam("email") String email,
                           @RequestParam("role") String role,
                           Model model) {
        try {
            userService.update(id, fullName, email, role);
            return "redirect:/superadmin/users?updated";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", userService.findById(id));
            model.addAttribute("roles", Role.values());
            return "superadmin/user-edit";
        }
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/superadmin/users?deleted";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("user", userService.getCurrentUser());

        // objets pour les formulaires (IMPORTANT pour ton HTML)
        model.addAttribute("profileReq", new ProfileRequest());
        model.addAttribute("passwordReq", new PasswordRequest());

        model.addAttribute("currentUrl", "/superadmin/profile");

        return "superadmin/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute("profileReq") ProfileRequest req,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Model model) {
        try {
            req.setAvatarFile(avatarFile);
            userService.updateProfile(req);
            return "redirect:/superadmin/profile?updated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", userService.getCurrentUser());
            model.addAttribute("passwordReq", new PasswordRequest());
            model.addAttribute("currentUrl", "/superadmin/profile");
            return "superadmin/profile";
        }
    }

    @PostMapping("/profile/password")
    public String changePassword(
            @ModelAttribute("passwordReq") PasswordRequest req,
            Model model) {
        try {
            userService.changePassword(req);
            return "redirect:/superadmin/profile?passwordChanged";
        } catch (Exception e) {
            model.addAttribute("passwordError", e.getMessage());
            model.addAttribute("user", userService.getCurrentUser());
            model.addAttribute("profileReq", new ProfileRequest());
            model.addAttribute("currentUrl", "/superadmin/profile");
            return "superadmin/profile";
        }
    }
}