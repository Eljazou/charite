package com.example.charite.repository;

import com.example.charite.entity.Organization;
import com.example.charite.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByTaxNumber(String taxNumber);
    boolean existsByCreatedBy(User createdBy);
    List<Organization> findByCreatedBy(User createdBy);
}