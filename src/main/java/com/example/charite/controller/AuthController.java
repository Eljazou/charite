package com.example.charite.controller;

import com.example.charite.dto.LoginRequest;
import com.example.charite.dto.RegisterRequest;
import com.example.charite.entity.User;
import com.example.charite.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User saved = userService.register(req);
            // Ne renvoie pas le password
            return ResponseEntity.ok("Compte créé avec succès (id=" + saved.getId() + ")");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(e.getMessage()); // 409 Conflict
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email, req.password)
        );

        // Si on arrive ici => OK
        return ResponseEntity.ok("Login OK: " + auth.getName());
    }
}