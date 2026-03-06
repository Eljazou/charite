package com.example.charite.repository;

import com.example.charite.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByTaxNumber(String taxNumber);
}