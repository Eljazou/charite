package com.example.charite.controller;

import com.example.charite.dto.CharityActionCreateRequest;
import com.example.charite.dto.DonationRequest;
import com.example.charite.entity.CharityAction;
import com.example.charite.enums.PaymentMethod;
import com.example.charite.service.CharityActionService;
import com.example.charite.service.DonationService;
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

    // ORG_ADMIN: list their actions
    @GetMapping("/my")
    public String myActions(Model model, Principal principal) {
        model.addAttribute("actions", charityActionService.findByOrganization(principal.getName()));
        model.addAttribute("currentUrl", "/actions/my");
        return "actions/my";
    }

    // ORG_ADMIN: create form
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("req", new CharityActionCreateRequest());
        return "actions/create";
    }

    // ORG_ADMIN: submit create
    @PostMapping("/create")
    public String create(@ModelAttribute("req") CharityActionCreateRequest req,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @RequestParam(value = "videos", required = false) List<MultipartFile> videos,
                         Principal principal,
                         Model model) {
        try {
            charityActionService.requestCreate(req, images, videos, principal.getName());
            return "redirect:/actions/my?submitted";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "actions/create";
        }
    }


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

    // Stripe success callback
    @GetMapping("/{id}/donate/success")
    public String stripeSuccess(@PathVariable Long id,
                                @RequestParam("session_id") String sessionId,
                                Model model) {
        donationService.confirmDonation(id, sessionId);
        model.addAttribute("action", charityActionService.findById(id));
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
        donationService.confirmBankDonation(donationId);
        return "redirect:/actions/" + id + "/donate/success-bank";
    }

    // Bank transfer success
    @GetMapping("/{id}/donate/success-bank")
    public String bankSuccess(@PathVariable Long id, Model model) {
        model.addAttribute("action", charityActionService.findById(id));
        return "actions/payment-success";
    }

    // PayPal page (coming soon)
    @GetMapping("/{id}/donate/paypal")
    public String paypalPage(@PathVariable Long id, Model model) {
        model.addAttribute("action", charityActionService.findById(id));
        return "actions/payment-paypal";
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
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        CharityAction action = charityActionService.findById(id);
        CharityActionCreateRequest req = new CharityActionCreateRequest();
        req.setTitle(action.getTitle());
        req.setDescription(action.getDescription());
        req.setLocation(action.getLocation());
        req.setStartDate(action.getStartDate());
        req.setEndDate(action.getEndDate());
        req.setGoalAmount(action.getGoalAmount());
        model.addAttribute("req", req);
        model.addAttribute("actionId", id);
        return "actions/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @ModelAttribute("req") CharityActionCreateRequest req,
                       @RequestParam(value = "images", required = false) List<MultipartFile> images,
                       @RequestParam(value = "videos", required = false) List<MultipartFile> videos,
                       Principal principal,
                       Model model) {
        try {
            charityActionService.requestUpdate(id, req, images, videos, principal.getName());
            return "redirect:/actions/my?updateRequested";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("actionId", id);
            return "actions/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteAction(@PathVariable Long id, Principal principal) {
        charityActionService.requestDelete(id, principal.getName());
        return "redirect:/actions/my?deleteRequested";
    }
    @GetMapping("/my-donations")
    public String myDonations(Model model, Principal principal) {
        model.addAttribute("donations", donationService.findByUser(principal.getName()));
        model.addAttribute("currentUrl", "/actions/my-donations");
        return "actions/my-donations";
    }
}