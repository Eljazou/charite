package com.example.charite.service;

import com.example.charite.dto.OrganizationCreateRequest;
import com.example.charite.dto.OrganizationUpdateRequest;
import com.example.charite.entity.Organization;
import com.example.charite.entity.PendingChange;
import com.example.charite.entity.User;
import com.example.charite.enums.PendingChangeStatus;
import com.example.charite.enums.PendingChangeType;
import com.example.charite.repository.OrganizationRepository;
import com.example.charite.repository.PendingChangeRepository;
import com.example.charite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PendingChangeService {

    private final PendingChangeRepository pendingChangeRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;


    public void requestCreate(OrganizationCreateRequest req, String email) {
        User caller = getUser(email);

        PendingChange change = PendingChange.builder()
                .requestedBy(caller)
                .type(PendingChangeType.CREATE)
                .newName(req.getName())
                .newLegalAddress(req.getLegalAddress())
                .newTaxNumber(req.getTaxNumber())
                .newMainContact(req.getMainContact())
                .newLogoUrl(req.getLogoUrl())
                .newDescription(req.getDescription())
                .build();

        pendingChangeRepository.save(change);
    }

    // ORG_ADMIN submits an update request
    public void requestUpdate(Long orgId, OrganizationUpdateRequest req, String email) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation introuvable"));
        User caller = getUser(email);

        PendingChange change = PendingChange.builder()
                .organization(org)
                .requestedBy(caller)
                .type(PendingChangeType.UPDATE)
                .newName(req.getName())
                .newLegalAddress(req.getLegalAddress())
                .newTaxNumber(req.getTaxNumber())
                .newMainContact(req.getMainContact())
                .newLogoUrl(req.getLogoUrl())
                .newDescription(req.getDescription())
                .build();

        pendingChangeRepository.save(change);
    }

    // ORG_ADMIN submits a delete request
    public void requestDelete(Long orgId, String email) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation introuvable"));
        User caller = getUser(email);

        PendingChange change = PendingChange.builder()
                .organization(org)
                .requestedBy(caller)
                .type(PendingChangeType.DELETE)
                .build();

        pendingChangeRepository.save(change);
    }

    // SUPER_ADMIN sees all pending requests
    public List<PendingChange> findAllPending() {
        List<PendingChange> list = pendingChangeRepository.findByStatus(PendingChangeStatus.PENDING);
        System.out.println("=== PENDING COUNT: " + list.size());
        return list;
    }

    // SUPER_ADMIN approves
    public void approve(Long changeId) {
        PendingChange change = getChange(changeId);

        if (change.getType() == PendingChangeType.CREATE) {
            User creator = change.getRequestedBy();

            Organization org = Organization.builder()
                    .name(change.getNewName())
                    .legalAddress(change.getNewLegalAddress())
                    .taxNumber(change.getNewTaxNumber())
                    .mainContact(change.getNewMainContact())
                    .logoUrl(change.getNewLogoUrl())
                    .description(change.getNewDescription())
                    .createdBy(creator)
                    .build();

            organizationRepository.save(org);

        } else if (change.getType() == PendingChangeType.DELETE) {
            organizationRepository.delete(change.getOrganization());

        } else if (change.getType() == PendingChangeType.UPDATE) {
            Organization org = change.getOrganization();
            org.setName(change.getNewName());
            org.setLegalAddress(change.getNewLegalAddress());
            org.setTaxNumber(change.getNewTaxNumber());
            org.setMainContact(change.getNewMainContact());
            org.setLogoUrl(change.getNewLogoUrl());
            org.setDescription(change.getNewDescription());
            organizationRepository.save(org);
        }

        change.setStatus(PendingChangeStatus.APPROVED);
        pendingChangeRepository.save(change);
    }

    // SUPER_ADMIN rejects
    public void reject(Long changeId) {
        PendingChange change = getChange(changeId);
        change.setStatus(PendingChangeStatus.REJECTED);
        pendingChangeRepository.save(change);
    }

    private PendingChange getChange(Long id) {
        return pendingChangeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}