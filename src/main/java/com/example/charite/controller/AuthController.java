package com.example.charite.controller;

import com.example.charite.dto.RegisterRequest;
import com.example.charite.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("req", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("req") RegisterRequest req, Model model) {
        try {
            userService.register(req);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}