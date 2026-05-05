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
            String locale = "fr".equals(donation.getUser().getLanguage()) ? "fr-FR" : "en-US"; // ← AJOUTER
            return paymentService.createPayPalOrder(actionId, req.getAmount(), locale); // ← MODIFIER
        } else {
            return "/actions/" + actionId + "/donate/bank?donationId=" + donation.getId();
        }
    }

    public void confirmDonation(Long actionId, String sessionId) {
        // Stripe confirmation — find donation by session or just the latest PENDING
        CharityAction action = charityActionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action introuvable"));

        // get the LAST pending donation for this action (most recent)
        donationRepository.findByCharityAction(action).stream()
                .filter(d -> d.getStatus() == DonationStatus.PENDING
                        && d.getPaymentMethod() == PaymentMethod.CREDIT_CARD)
                .reduce((first, second) -> second) // take the last one
                .ifPresent(d -> {
                    d.setStatus(DonationStatus.COMPLETED);
                    donationRepository.save(d);
                    action.setCollectedAmount(action.getCollectedAmount().add(d.getAmount()));
                    charityActionRepository.save(action);
                });
    }

    // confirm bank transfer manually
    public void confirmBankDonation(Long donationId) {
        System.out.println("=== CONFIRMING DONATION ID: " + donationId);
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Don introuvable"));
        System.out.println("=== DONATION STATUS BEFORE: " + donation.getStatus());
        donation.setStatus(DonationStatus.COMPLETED);
        donationRepository.save(donation);
        System.out.println("=== DONATION STATUS AFTER: " + donation.getStatus());

        CharityAction action = donation.getCharityAction();
        action.setCollectedAmount(action.getCollectedAmount().add(donation.getAmount()));
        charityActionRepository.save(action);
    }
    public List<Donation> findByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return donationRepository.findByUser(user);
    }
    public Donation getLastCompletedDonation(Long actionId) {
        CharityAction action = charityActionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action introuvable"));
        return donationRepository.findByCharityAction(action).stream()
                .filter(d -> d.getStatus() == DonationStatus.COMPLETED)
                .reduce((first, second) -> second)
                .orElse(null);
    }


    public void confirmPayPalDonation(Long actionId) {
        CharityAction action = charityActionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action introuvable"));

        donationRepository.findByCharityAction(action).stream()
                .filter(d -> d.getStatus() == DonationStatus.PENDING
                        && d.getPaymentMethod() == PaymentMethod.PAYPAL)
                .reduce((first, second) -> second)
                .ifPresent(d -> {
                    d.setStatus(DonationStatus.COMPLETED);
                    donationRepository.save(d);
                    action.setCollectedAmount(action.getCollectedAmount().add(d.getAmount()));
                    charityActionRepository.save(action);
                });
    }


    public Donation findById(Long donationId) {
        return donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Don introuvable"));
    }

    public void cancelDonation(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Don introuvable"));
        donation.setStatus(DonationStatus.FAILED);
        donationRepository.save(donation);
    }

    public String resumeDonation(Long actionId, Long donationId) throws Exception {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Don introuvable"));

        if (donation.getPaymentMethod() == PaymentMethod.PAYPAL) {
            String locale = "fr".equals(donation.getUser().getLanguage()) ? "fr-FR" : "en-US";// ← AJOUTER
            return paymentService.createPayPalOrder(actionId, donation.getAmount(), locale); // ← MODIFIER
        } else if (donation.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
            return paymentService.createStripeSession(actionId, donation.getAmount(), "mad");
        } else {
            return "/actions/" + actionId + "/donate/bank?donationId=" + donationId;
        }
    }
}