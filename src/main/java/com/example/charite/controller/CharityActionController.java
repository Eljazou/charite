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

    // USER: list active actions
    @GetMapping
    public String activeActions(Model model) {
        model.addAttribute("actions", charityActionService.findActiveActions());
        return "actions/list";
    }

    // USER: donate form
    @GetMapping("/{id}/donate")
    public String donateForm(@PathVariable Long id, Model model) {
        CharityAction action = charityActionService.findById(id);
        model.addAttribute("action", action);
        model.addAttribute("req", new DonationRequest());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "actions/donate";
    }

    // USER: submit donation
    @PostMapping("/{id}/donate")
    public String donate(@PathVariable Long id,
                         @ModelAttribute("req") DonationRequest req,
                         Principal principal,
                         Model model) {
        try {
            donationService.donate(id, req, principal.getName());
            return "redirect:/actions?donated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "actions/donate";
        }
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CharityAction action = charityActionService.findByIdActive(id);
        model.addAttribute("action", action);
        model.addAttribute("donationCount", charityActionService.countDonations(id));
        return "actions/detail";
    }
}