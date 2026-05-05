package com.example.charite.controller;

import com.example.charite.dto.CharityActionCreateRequest;
import com.example.charite.dto.DonationRequest;
import com.example.charite.entity.CharityAction;
import com.example.charite.entity.Donation;
import com.example.charite.enums.CharityActionStatus;
import com.example.charite.enums.PaymentMethod;
import com.example.charite.service.CharityActionService;
import com.example.charite.service.DonationService;
import com.example.charite.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/actions")
@RequiredArgsConstructor
public class CharityActionController {

    private final CharityActionService charityActionService;
    private final DonationService donationService;
    private final PaymentService paymentService;

    @PostMapping("/{id}/donate")
    public String donate(@PathVariable Long id,
                         @ModelAttribute("req") DonationRequest req,
                         Principal principal,
                         Model model) {
        try {
            String redirectUrl = donationService.initDonation(id, req, principal.getName());
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            CharityAction action = charityActionService.findById(id);
            model.addAttribute("action", action);
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "actions/donate";
        }
    }
    //add by chatgpt
    @GetMapping("/{id}/donate")
    public String showDonatePage(@PathVariable Long id, Model model) {
        model.addAttribute("action", charityActionService.findById(id));
        model.addAttribute("req", new DonationRequest());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "actions/donate";
    }

    @GetMapping("/{id}/donate/success")
    public String stripeSuccess(@PathVariable Long id,
                                @RequestParam("session_id") String sessionId,
                                Model model) {
        donationService.confirmDonation(id, sessionId);
        CharityAction action = charityActionService.findById(id);

        // get the last completed donation for this action
        Donation donation = donationService.getLastCompletedDonation(id);

        model.addAttribute("action", action);
        model.addAttribute("transactionId", "TX-" + (donation != null ? donation.getId() : sessionId.substring(0, 8)));
        model.addAttribute("amount", donation != null ? donation.getAmount() : "N/A");
        return "actions/payment-success";
    }
    // Stripe cancel callback
    @GetMapping("/{id}/donate/cancel")
    public String stripeCancel(@PathVariable Long id, Model model) {
        model.addAttribute("action", charityActionService.findById(id));
        return "actions/payment-cancel";
    }

    // Bank transfer page
    @GetMapping("/{id}/donate/bank")
    public String bankTransfer(@PathVariable Long id,
                               @RequestParam("donationId") Long donationId,
                               Model model) {
        model.addAttribute("action", charityActionService.findById(id));
        model.addAttribute("donationId", donationId);
        return "actions/payment-bank";
    }

    // Bank transfer confirm
    @PostMapping("/{id}/donate/bank/confirm")
    public String confirmBank(@PathVariable Long id,
                              @RequestParam("donationId") Long donationId) {
        System.out.println("=== CONFIRM BANK CALLED ===");
        System.out.println("=== Action ID: " + id);
        System.out.println("=== Donation ID: " + donationId);
        donationService.confirmBankDonation(donationId);
        return "redirect:/actions/" + id + "/donate/success-bank";
    }
    // Bank transfer success
    @GetMapping("/{id}/donate/success-bank")
    public String bankSuccess(@PathVariable Long id, Model model) {
        CharityAction action = charityActionService.findById(id);
        Donation donation = donationService.getLastCompletedDonation(id);

        model.addAttribute("action", action);
        model.addAttribute("transactionId", "TX-" + (donation != null ? donation.getId() : System.currentTimeMillis()));
        model.addAttribute("amount", donation != null ? donation.getAmount() : "N/A");
        return "actions/payment-success";
    }

    // PayPal success callback
    @GetMapping("/{id}/donate/paypal/success")
    public String paypalSuccess(@PathVariable Long id,
                                @RequestParam("token") String orderId,
                                Model model) {
        try {
            paymentService.capturePayPalOrder(orderId);
            donationService.confirmPayPalDonation(id);
            Donation donation = donationService.getLastCompletedDonation(id);
            model.addAttribute("action", charityActionService.findById(id));
            model.addAttribute("transactionId", "TX-" + (donation != null ? donation.getId() : orderId.substring(0, 8)));
            model.addAttribute("amount", donation != null ? donation.getAmount() : "N/A");
            return "actions/payment-success";
        } catch (Exception e) {
            return "redirect:/actions/" + id + "/donate/cancel";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CharityAction action = charityActionService.findByIdActive(id);
        model.addAttribute("action", action);
        model.addAttribute("donationCount", charityActionService.countDonations(id));
        return "actions/detail";
    }
    // USER: list active actions avec recherche
    @GetMapping
    public String activeActions(@RequestParam(value = "q", required = false) String query,
                                Model model) {
        model.addAttribute("actions", charityActionService.search(query));
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("currentUrl", "/actions");
        return "actions/list";
    }

    @GetMapping("/my-donations")
    public String myDonations(Model model, Principal principal) {
        model.addAttribute("donations", donationService.findByUser(principal.getName()));
        model.addAttribute("currentUrl", "/actions/my-donations");
        return "actions/my-donations";
    }



    // Afficher la page resume
    @GetMapping("/{id}/donate/resume")
    public String resumeDonationPage(@PathVariable Long id,
                                     @RequestParam("donationId") Long donationId,
                                     Model model) {
        model.addAttribute("action", charityActionService.findById(id));
        model.addAttribute("donation", donationService.findById(donationId));
        return "actions/resume-donation";
    }

    // Continuer le paiement
    @PostMapping("/{id}/donate/resume")
    public String resumePayment(@PathVariable Long id,
                                @RequestParam("donationId") Long donationId,
                                Model model) {
        try {
            String redirectUrl = donationService.resumeDonation(id, donationId);
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/actions/" + id + "/donate/resume?donationId=" + donationId;
        }
    }

    // Annuler le don → FAILED
    @PostMapping("/{id}/donate/cancel-donation")
    public String cancelDonation(@PathVariable Long id,
                                 @RequestParam("donationId") Long donationId) {
        donationService.cancelDonation(donationId);
        return "redirect:/actions/my-donations";
    }

}