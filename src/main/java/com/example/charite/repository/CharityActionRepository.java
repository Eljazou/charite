package com.example.charite.repository;

import com.example.charite.entity.CharityAction;
import com.example.charite.entity.Organization;
import com.example.charite.enums.CharityActionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CharityActionRepository extends JpaRepository<CharityAction, Long> {
    List<CharityAction> findByOrganization(Organization organization);
    List<CharityAction> findByStatus(CharityActionStatus status);
}