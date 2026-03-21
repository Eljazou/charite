package com.example.charite.entity;

import com.example.charite.enums.CharityActionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "charity_actions")
public class CharityAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 3000)
    private String description;

    private String location;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private BigDecimal goalAmount;

    @Builder.Default
    private BigDecimal collectedAmount = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CharityActionStatus status = CharityActionStatus.PENDING_APPROVAL;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Builder.Default
    @OneToMany(mappedBy = "charityAction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CharityActionMedia> mediaList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "charityAction", cascade = CascadeType.ALL)
    private List<Donation> donations = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = CharityActionStatus.PENDING_APPROVAL;
        if (collectedAmount == null) collectedAmount = BigDecimal.ZERO;
    }
}