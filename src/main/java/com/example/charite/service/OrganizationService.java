package com.example.charite.service;

import com.example.charite.dto.OrganizationCreateRequest;
import com.example.charite.entity.Organization;
import com.example.charite.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public Organization create(OrganizationCreateRequest req) {

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
                .build();

        return organizationRepository.save(org);
    }
}