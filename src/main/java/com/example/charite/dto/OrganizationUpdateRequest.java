package com.example.charite.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class OrganizationUpdateRequest {
    private String name;
    private String legalAddress;
    private String taxNumber;
    private String mainContact;
    private String logoUrl;
    private String description;
}