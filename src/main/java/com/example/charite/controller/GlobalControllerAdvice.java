package com.example.charite.controller;

import com.example.charite.entity.Organization;
import com.example.charite.entity.User;
import com.example.charite.repository.OrganizationRepository;
import com.example.charite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    @ModelAttribute("currentOrg")
    public Organization currentOrg(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        return userRepository.findByEmail(authentication.getName())
                .flatMap(organizationRepository::findByCreatedBy)
                .orElse(null);
    }
    @ModelAttribute("currentUser")
    public User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }
}