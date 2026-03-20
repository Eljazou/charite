package com.example.charite.repository;

import com.example.charite.entity.PendingChange;
import com.example.charite.entity.User;
import com.example.charite.enums.PendingChangeStatus;
import com.example.charite.enums.PendingChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PendingChangeRepository extends JpaRepository<PendingChange, Long> {
    List<PendingChange> findByStatus(PendingChangeStatus status);
    boolean existsByRequestedByAndTypeAndStatus(User requestedBy, PendingChangeType type, PendingChangeStatus status);
}