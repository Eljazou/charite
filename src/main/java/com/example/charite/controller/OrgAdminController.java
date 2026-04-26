package com.example.charite.controller;

import com.example.charite.dto.*;
import com.example.charite.entity.CharityAction;
import com.example.charite.entity.Organization;
import com.example.charite.enums.CharityActionStatus;
import com.example.charite.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/orgadmin")
@RequiredArgsConstructor
public class OrgAdminController {

    private final UserService userService;
    private final OrganizationService organizationService;
    private final CharityActionService charityActionService;
    private final DashboardService dashboardService;
    private final PendingChangeService pendingChangeService;

    // ─── DASHBOARD ───────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        model.addAttribute("stats", dashboardService.getStats(principal.getName()));
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("currentUrl", "/orgadmin/dashboard");
        return "orgadmin/dashboard";
    }

    // ─── PROFILE ─────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("user", userService.getCurrentUser());
        model.addAttribute("currentUser", userService.getCurrentUser()); // ← ajoute
        model.addAttribute("profileReq", new ProfileRequest());
        model.addAttribute("passwordReq", new PasswordRequest());
        model.addAttribute("currentUrl", "/orgadmin/profile");
        return "orgadmin/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("profileReq") ProfileRequest req,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                Model model) {
        try {
            req.setAvatarFile(avatarFile);
            userService.updateProfile(req);
            return "redirect:/orgadmin/profile?updated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", userService.getCurrentUser());
            model.addAttribute("currentUser", userService.getCurrentUser());
            model.addAttribute("passwordReq", new PasswordRequest());
            model.addAttribute("currentUrl", "/orgadmin/profile");
            return "orgadmin/profile";
        }
    }

    @PostMapping("/profile/password")
    public String changePassword(@ModelAttribute("passwordReq") PasswordRequest req,
                                 Model model) {
        try {
            userService.changePassword(req);
            return "redirect:/orgadmin/profile?passwordChanged";
        } catch (Exception e) {
            model.addAttribute("passwordError", e.getMessage());
            model.addAttribute("user", userService.getCurrentUser());
            model.addAttribute("currentUser", userService.getCurrentUser());
            model.addAttribute("profileReq", new ProfileRequest());
            model.addAttribute("currentUrl", "/orgadmin/profile");
            return "orgadmin/profile";
        }
    }

    // ─── ORGANIZATIONS ───────────────────────────────────────
    @GetMapping("/organizations")
    public String organizations(Principal principal, Model model) {
        model.addAttribute("organizations", organizationService.findByUser(principal.getName()));
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("currentUrl", "/orgadmin/organizations");
        return "orgadmin/organizations";
    }

    @GetMapping("/organizations/create")
    public String createOrganizationForm(Model model) {
        model.addAttribute("req", new OrganizationCreateRequest());
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("currentUrl", "/orgadmin/organizations");
        return "orgadmin/organization-create";
    }

    @PostMapping("/organizations/create")
    public String createOrganization(@ModelAttribute("req") OrganizationCreateRequest req,
                                     @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                                     Principal principal, Model model) {
        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + logoFile.getOriginalFilename();
                Path uploadPath = Path.of("uploads/logos");
                Files.createDirectories(uploadPath);
                Files.copy(logoFile.getInputStream(), uploadPath.resolve(fileName));
                req.setLogoUrl("/logos/" + fileName);
            }
            organizationService.create(req, principal.getName());
            return "redirect:/orgadmin/organizations?created";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("currentUser", userService.getCurrentUser());
            return "orgadmin/organization-create";
        }
    }

    @GetMapping("/organizations/edit/{id}")
    public String editOrganizationForm(@PathVariable Long id, Model model) {
        Organization org = organizationService.findById(id);
        OrganizationUpdateRequest req = new OrganizationUpdateRequest();
        req.setName(org.getName());
        req.setLegalAddress(org.getLegalAddress());
        req.setTaxNumber(org.getTaxNumber());
        req.setMainContact(org.getMainContact());
        req.setLogoUrl(org.getLogoUrl());
        req.setDescription(org.getDescription());
        model.addAttribute("req", req);
        model.addAttribute("orgId", id);
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("currentUrl", "/orgadmin/organizations");
        return "orgadmin/organization-edit";
    }

    @PostMapping("/organizations/edit/{id}")
    public String editOrganization(@PathVariable Long id,
                                   @ModelAttribute("req") OrganizationUpdateRequest req,
                                   @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                                   Principal principal, Model model) {
        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + logoFile.getOriginalFilename();
                Path uploadPath = Path.of("uploads/logos");
                Files.createDirectories(uploadPath);
                Files.copy(logoFile.getInputStream(), uploadPath.resolve(fileName));
                req.setLogoUrl("/logos/" + fileName);
            }
            pendingChangeService.requestUpdate(id, req, principal.getName());
            return "redirect:/orgadmin/organizations?updated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("currentUser", userService.getCurrentUser());
            return "orgadmin/organization-edit";
        }
    }

    @PostMapping("/organizations/delete/{id}")
    public String deleteOrganization(@PathVariable Long id, Principal principal) {
        pendingChangeService.requestDelete(id, principal.getName());
        return "redirect:/orgadmin/organizations?deleted";
    }

    // ─── ACTIONS ─────────────────────────────────────────────
    @GetMapping("/actions")
    public String actions(Principal principal, Model model) {
        model.addAttribute("actions", charityActionService.findByOrganization(principal.getName()));
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("currentUrl", "/orgadmin/actions");
        return "orgadmin/actions";
    }

    @GetMapping("/actions/create")
    public String createActionForm(Model model) {
        model.addAttribute("req", new CharityActionCreateRequest());
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("currentUrl", "/orgadmin/actions");
        return "orgadmin/action-create";
    }

    @PostMapping("/actions/create")
    public String createAction(@ModelAttribute("req") CharityActionCreateRequest req,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               @RequestParam(value = "videos", required = false) List<MultipartFile> videos,
                               Principal principal, Model model) {
        try {
            charityActionService.requestCreate(req, images, videos, principal.getName());
            return "redirect:/orgadmin/actions?created";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("currentUser", userService.getCurrentUser());
            return "orgadmin/action-create";
        }
    }

    @GetMapping("/actions/edit/{id}")
    public String editActionForm(@PathVariable Long id, Model model) {
        CharityAction action = charityActionService.findById(id);
        CharityActionCreateRequest req = new CharityActionCreateRequest();
        req.setTitle(action.getTitle());
        req.setDescription(action.getDescription());
        req.setLocation(action.getLocation());
        req.setStartDate(action.getStartDate());
        req.setEndDate(action.getEndDate());
        req.setGoalAmount(action.getGoalAmount());
        req.setStatus(action.getStatus());
        model.addAttribute("req", req);
        model.addAttribute("actionId", id);
        model.addAttribute("currentMedias", action.getMediaList());
        model.addAttribute("statuses", CharityActionStatus.values());
        model.addAttribute("currentUser", userService.getCurrentUser());
        model.addAttribute("currentUrl", "/orgadmin/actions");
        return "orgadmin/action-edit";
    }

    @PostMapping("/actions/edit/{id}")
    public String editAction(@PathVariable Long id,
                             @ModelAttribute("req") CharityActionCreateRequest req,
                             @RequestParam(value = "images", required = false) List<MultipartFile> images,
                             @RequestParam(value = "videos", required = false) List<MultipartFile> videos,
                             Principal principal, Model model) {
        try {
            charityActionService.requestUpdate(id, req, images, videos, principal.getName());
            return "redirect:/orgadmin/actions?updated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("actionId", id);
            model.addAttribute("currentUser", userService.getCurrentUser());
            return "orgadmin/action-edit";
        }
    }

    @PostMapping("/actions/delete/{id}")
    public String deleteAction(@PathVariable Long id, Principal principal) {
        charityActionService.requestDelete(id, principal.getName());
        return "redirect:/orgadmin/actions?deleted";
    }

    @PostMapping("/actions/media/delete/{mediaId}")
    public String deleteMedia(@PathVariable Long mediaId,
                              @RequestParam("actionId") Long actionId) {
        charityActionService.deleteMedia(mediaId);
        return "redirect:/orgadmin/actions/edit/" + actionId;
    }
}