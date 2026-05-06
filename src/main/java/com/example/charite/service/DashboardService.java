package com.example.charite.service;

import com.example.charite.dto.DashboardStats;
import com.example.charite.entity.*;
import com.example.charite.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final CharityActionRepository charityActionRepository;
    private final DonationRepository donationRepository;

    @Transactional
    public DashboardStats getStats(String email) {
        User caller = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<Organization> orgOpt = organizationRepository.findByCreatedBy(caller);

        if (orgOpt.isEmpty()) {
            return DashboardStats.builder()
                    .organization(null)
                    .totalActions(0)
                    .totalCollected(BigDecimal.ZERO)
                    .totalGoal(BigDecimal.ZERO)
                    .actions(List.of())
                    .recentDonations(List.of())
                    .donationsByPaymentMethod(Map.of())
                    .actionProgress(Map.of())
                    .build();
        }

        Organization org = orgOpt.get();
        List<CharityAction> actions = charityActionRepository.findByOrganization(org);

        charityActionRepository.closeExpiredActions(LocalDate.now());
        actions = charityActionRepository.findByOrganization(org);

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

        Map<Long, Integer> actionProgress = actions.stream()
                .collect(Collectors.toMap(
                        CharityAction::getId,
                        a -> a.getGoalAmount().compareTo(BigDecimal.ZERO) > 0
                                ? a.getCollectedAmount()
                                .multiply(new BigDecimal(100))
                                .divide(a.getGoalAmount(), 0, java.math.RoundingMode.HALF_UP)
                                .intValue()
                                : 0
                ));

        return DashboardStats.builder()
                .organization(org)
                .totalActions((long) actions.size())
                .totalCollected(totalCollected)
                .totalGoal(totalGoal)
                .actions(actions)
                .recentDonations(recentDonations)
                .donationsByPaymentMethod(donationsByPaymentMethod)
                .actionProgress(actionProgress)
                .build();
    }
}