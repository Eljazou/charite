package com.example.charite.service;

import com.example.charite.dto.RegisterRequest;
import com.example.charite.enums.Role;
import com.example.charite.entity.User;
import com.example.charite.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        User u = new User();
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.USER);

        return userRepository.save(u);
    }
}