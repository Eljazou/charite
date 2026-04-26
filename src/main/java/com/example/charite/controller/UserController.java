package com.example.charite.controller;

import org.springframework.web.multipart.MultipartFile;
import com.example.charite.dto.PasswordRequest;
import com.example.charite.dto.ProfileRequest;
import com.example.charite.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("user", userService.getCurrentUser());
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("profileReq", new ProfileRequest());
        model.addAttribute("passwordReq", new PasswordRequest());
        model.addAttribute("currentUrl", "/user/profile");
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("profileReq") ProfileRequest req,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                Model model) {
        try {
            req.setAvatarFile(avatarFile);
            userService.updateProfile(req);
            return "redirect:/user/profile?updated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", userService.getCurrentUser());
            model.addAttribute("currentUser", userService.getCurrentUser());
            model.addAttribute("passwordReq", new PasswordRequest());
            model.addAttribute("currentUrl", "/user/profile");
            return "user/profile";
        }
    }

    @PostMapping("/profile/password")
    public String changePassword(@ModelAttribute("passwordReq") PasswordRequest req,
                                 Model model) {
        try {
            userService.changePassword(req);
            return "redirect:/user/profile?passwordChanged";
        } catch (Exception e) {
            model.addAttribute("passwordError", e.getMessage());
            model.addAttribute("user", userService.getCurrentUser());
            model.addAttribute("currentUser", userService.getCurrentUser());
            model.addAttribute("profileReq", new ProfileRequest());
            model.addAttribute("currentUrl", "/user/profile");
            return "user/profile";
        }
    }
}
