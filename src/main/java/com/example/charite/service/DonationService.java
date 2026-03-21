package com.example.charite.service;

import com.example.charite.dto.DonationRequest;
import com.example.charite.entity.*;
import com.example.charite.enums.DonationStatus;
import com.example.charite.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final CharityActionRepository charityActionRepository;
    private final UserRepository userRepository;

    public void donate(Long actionId, DonationRequest req, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CharityAction action = charityActionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action introuvable"));

        Donation donation = Donation.builder()
                .amount(req.getAmount())
                .message(req.getMessage())
                .paymentMethod(req.getPaymentMethod())
                .charityAction(action)
                .user(user)
                .status(DonationStatus.COMPLETED)
                .build();

        donationRepository.save(donation);

        // update collected amount
        action.setCollectedAmount(action.getCollectedAmount().add(req.getAmount()));
        charityActionRepository.save(action);
    }
}