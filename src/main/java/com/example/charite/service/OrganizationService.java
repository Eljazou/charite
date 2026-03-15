package com.example.charite.service;

import com.example.charite.dto.OrganizationCreateRequest;
import com.example.charite.entity.Organization;
import com.example.charite.entity.User;
import com.example.charite.repository.OrganizationRepository;
import com.example.charite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public Organization create(OrganizationCreateRequest req, String email) {

        User caller = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (organizationRepository.existsByTaxNumber(req.getTaxNumber())) {
            throw new IllegalArgumentException("Tax number déjà utilisé");
        }

        Organization org = Organization.builder()
                .name(req.getName())
                .legalAddress(req.getLegalAddress())
                .taxNumber(req.getTaxNumber())
                .mainContact(req.getMainContact())
                .logoUrl(req.getLogoUrl())
                .description(req.getDescription())
                .createdBy(caller)
                .build();

        return organizationRepository.save(org);
    }

    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }
}