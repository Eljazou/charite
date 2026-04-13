package com.example.charite.service;

import com.example.charite.dto.DonationRequest;
import com.example.charite.entity.*;
import com.example.charite.enums.DonationStatus;
import com.example.charite.enums.PaymentMethod;
import com.example.charite.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final CharityActionRepository charityActionRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    // create pending donation and return redirect URL
    public String initDonation(Long actionId, DonationRequest req, String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CharityAction action = charityActionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action introuvable"));

        // save donation as PENDING first
        Donation donation = Donation.builder()
                .amount(req.getAmount())
                .message(req.getMessage())
                .paymentMethod(req.getPaymentMethod())
                .charityAction(action)
                .user(user)
                .status(DonationStatus.PENDING)
                .build();

        donationRepository.save(donation);

        // redirect based on payment method
        if (req.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
            return paymentService.createStripeSession(actionId, req.getAmount(), "mad");
        } else if (req.getPaymentMethod() == PaymentMethod.PAYPAL) {
            // PayPal coming soon — for now redirect to a coming soon page
            return "/actions/" + actionId + "/donate/paypal";
        } else {
            // BANK_TRANSFER — show bank details page
            return "/actions/" + actionId + "/donate/bank?donationId=" + donation.getId();
        }
    }

    // confirm donation after successful payment
    public void confirmDonation(Long actionId, String sessionId) {
        // find pending donation for this action and mark as completed
        CharityAction action = charityActionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action introuvable"));

        donationRepository.findByCharityAction(action).stream()
                .filter(d -> d.getStatus() == DonationStatus.PENDING)
                .findFirst()
                .ifPresent(d -> {
                    d.setStatus(DonationStatus.COMPLETED);
                    donationRepository.save(d);
                    action.setCollectedAmount(action.getCollectedAmount().add(d.getAmount()));
                    charityActionRepository.save(action);
                });
    }

    // confirm bank transfer manually
    public void confirmBankDonation(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Don introuvable"));
        donation.setStatus(DonationStatus.COMPLETED);
        donationRepository.save(donation);

        CharityAction action = donation.getCharityAction();
        action.setCollectedAmount(action.getCollectedAmount().add(donation.getAmount()));
        charityActionRepository.save(action);
    }
    public List<Donation> findByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return donationRepository.findByUser(user);
    }
}