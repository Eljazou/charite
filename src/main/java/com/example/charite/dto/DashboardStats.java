package com.example.charite.dto;

import com.example.charite.entity.CharityAction;
import com.example.charite.entity.Donation;
import com.example.charite.entity.Organization;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DashboardStats {
    private Organization organization;
    private long totalActions;
    private BigDecimal totalCollected;
    private BigDecimal totalGoal;
    private List<CharityAction> actions;
    private List<Donation> recentDonations;
    private Map<String, Long> donationsByPaymentMethod;
    private Map<Long, Integer> actionProgress;
}