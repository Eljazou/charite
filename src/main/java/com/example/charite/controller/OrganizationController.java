package com.example.charite.controller;

import com.example.charite.dto.OrganizationCreateRequest;
import com.example.charite.entity.Organization;
import com.example.charite.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public Organization create(@Valid @RequestBody OrganizationCreateRequest req) {
        return organizationService.create(req);
    }
}