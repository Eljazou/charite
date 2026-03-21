package com.example.charite.repository;

import com.example.charite.entity.Organization;
import com.example.charite.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByTaxNumber(String taxNumber);
    boolean existsByCreatedBy(User createdBy);
    Optional<Organization> findByCreatedBy(User createdBy);
}