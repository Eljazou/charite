package com.example.charite.entity;

import com.example.charite.enums.PendingChangeStatus;
import com.example.charite.enums.PendingChangeType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pending_changes")
public class PendingChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = true)
    private Organization organization;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PendingChangeType type;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PendingChangeStatus status = PendingChangeStatus.PENDING;

    // new values for UPDATE (null if DELETE)
    private String newName;
    private String newLegalAddress;
    private String newTaxNumber;
    private String newMainContact;
    private String newLogoUrl;

    @Column(length = 2000)
    private String newDescription;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();
    // pour CREATE_ACTION
    private String actionTitle;

    @Column(length = 3000)
    private String actionDescription;

    private String actionLocation;
    private LocalDate actionStartDate;
    private LocalDate actionEndDate;
    private BigDecimal actionGoalAmount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "charity_action_id", nullable = true)
    private CharityAction charityAction;
}