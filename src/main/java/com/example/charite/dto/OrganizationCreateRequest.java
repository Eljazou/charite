package com.example.charite.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String legalAddress;

    @NotBlank
    private String taxNumber;

    @NotBlank
    private String mainContact;

    private String logoUrl;

    private String description;
}