package com.example.charite.service;

import com.example.charite.dto.CharityActionCreateRequest;
import com.example.charite.entity.*;
import com.example.charite.enums.*;
import com.example.charite.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CharityActionService {

    private final CharityActionRepository charityActionRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PendingChangeRepository pendingChangeRepository;
    private final DonationRepository donationRepository;

    public void requestCreate(CharityActionCreateRequest req,
                              List<MultipartFile> images,
                              List<MultipartFile> videos,
                              String email) throws Exception {

        User caller = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Organization org = organizationRepository.findByCreatedBy(caller)
                .orElseThrow(() -> new IllegalArgumentException("Vous n'avez pas d'organisation"));

        // save media files temporarily in a temp action
        CharityAction tempAction = CharityAction.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .location(req.getLocation())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .goalAmount(req.getGoalAmount())
                .organization(org)
                .status(CharityActionStatus.PENDING_APPROVAL)
                .build();

        charityActionRepository.save(tempAction);

        // save images
        if (images != null) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path uploadPath = Path.of("uploads/actions/images");
                    Files.createDirectories(uploadPath);
                    Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
                    CharityActionMedia media = CharityActionMedia.builder()
                            .mediaType(MediaType.IMAGE)
                            .filePath("/actions/images/" + fileName)
                            .charityAction(tempAction)
                            .build();
                    tempAction.getMediaList().add(media);
                }
            }
        }

        // save videos
        if (videos != null) {
            for (MultipartFile file : videos) {
                if (!file.isEmpty()) {
                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path uploadPath = Path.of("uploads/actions/videos");
                    Files.createDirectories(uploadPath);
                    Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
                    CharityActionMedia media = CharityActionMedia.builder()
                            .mediaType(MediaType.VIDEO)
                            .filePath("/actions/videos/" + fileName)
                            .charityAction(tempAction)
                            .build();
                    tempAction.getMediaList().add(media);
                }
            }
        }

        charityActionRepository.save(tempAction);

        // create pending change for super admin
        PendingChange change = PendingChange.builder()
                .requestedBy(caller)
                .type(PendingChangeType.CREATE_ACTION)
                .charityAction(tempAction)
                .actionTitle(req.getTitle())
                .build();

        pendingChangeRepository.save(change);
    }

    public List<CharityAction> findActiveActions() {
        return charityActionRepository.findByStatus(CharityActionStatus.ACTIVE);
    }

    public List<CharityAction> findByOrganization(String email) {
        User caller = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Organization org = organizationRepository.findByCreatedBy(caller)
                .orElseThrow(() -> new IllegalArgumentException("Pas d'organisation"));
        return charityActionRepository.findByOrganization(org);
    }

    public CharityAction findById(Long id) {
        return charityActionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Action introuvable"));
    }
    public CharityAction findByIdActive(Long id) {
        CharityAction action = charityActionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Action introuvable"));
        if (action.getStatus() != CharityActionStatus.ACTIVE) {
            throw new IllegalArgumentException("Action non disponible");
        }
        return action;
    }

    public long countDonations(Long actionId) {
        CharityAction action = charityActionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action introuvable"));
        return donationRepository.findByCharityAction(action).size();
    }
}