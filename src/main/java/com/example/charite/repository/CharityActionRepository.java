package com.example.charite.repository;

import com.example.charite.entity.CharityAction;
import com.example.charite.entity.Organization;
import com.example.charite.enums.CharityActionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

import java.util.List;

public interface CharityActionRepository extends JpaRepository<CharityAction, Long> {
    List<CharityAction> findByOrganization(Organization organization);
    List<CharityAction> findByStatus(CharityActionStatus status);
    @Query("SELECT a FROM CharityAction a WHERE a.status = 'ACTIVE' AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "CAST(a.startDate AS string) LIKE CONCAT('%', :query, '%') OR " +
            "CAST(a.endDate AS string) LIKE CONCAT('%', :query, '%'))")
    List<CharityAction> searchActive(@Param("query") String query);

    @Modifying
    @Query("UPDATE CharityAction a SET a.status = 'CLOSED' " +
            "WHERE a.status = 'ACTIVE' " +
            "AND a.endDate IS NOT NULL " +
            "AND a.endDate < :today")
    int closeExpiredActions(@Param("today") LocalDate today);
}