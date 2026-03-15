package com.example.charite.entity;

import com.example.charite.enums.OrganizationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "organizations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "tax_number")
        }
)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String legalAddress;

    @Column(name = "tax_number", nullable = false)
    private String taxNumber;

    @Column(nullable = false)
    private String mainContact;

    private String logoUrl;

    @Column(length = 2000)
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationStatus status = OrganizationStatus.PENDING;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // who created this organization (must be ORG_ADMIN)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = OrganizationStatus.PENDING;
    }
}