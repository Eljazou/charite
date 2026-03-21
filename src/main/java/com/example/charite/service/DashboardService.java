package com.example.charite.service;

import com.example.charite.dto.DashboardStats;
import com.example.charite.entity.*;
import com.example.charite.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final CharityActionRepository charityActionRepository;
    private final DonationRepository donationRepository;

    public DashboardStats getStats(String email) {
        User caller = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Organization org = organizationRepository.findByCreatedBy(caller)
                .orElseThrow(() -> new IllegalArgumentException("Pas d'organisation"));

        List<CharityAction> actions = charityActionRepository.findByOrganization(org);

        BigDecimal totalCollected = actions.stream()
                .map(CharityAction::getCollectedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGoal = actions.stream()
                .map(CharityAction::getGoalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Donation> recentDonations = donationRepository.findRecentByOrganization(org);

        Map<String, Long> donationsByPaymentMethod = recentDonations.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getPaymentMethod().name(),
                        Collectors.counting()
                ));

        return DashboardStats.builder()
                .organization(org)
                .totalActions((long) actions.size())
                .totalCollected(totalCollected)
                .totalGoal(totalGoal)
                .actions(actions)
                .recentDonations(recentDonations)
                .donationsByPaymentMethod(donationsByPaymentMethod)
                .build();
    }
}