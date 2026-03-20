package com.example.charite.service;

import com.example.charite.dto.OrganizationCreateRequest;
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
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PendingChangeRepository pendingChangeRepository;

    public void create(OrganizationCreateRequest req, String email) {
        User caller = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));


        if (pendingChangeRepository.existsByRequestedByAndTypeAndStatus(
                caller, PendingChangeType.CREATE, PendingChangeStatus.PENDING)) {
            throw new IllegalArgumentException("Vous avez déjà une demande de création en attente");
        }

        if (organizationRepository.existsByTaxNumber(req.getTaxNumber())) {
            throw new IllegalArgumentException("Tax number déjà utilisé");
        }

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

    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }
    public List<Organization> findByUser(String email) {
        User caller = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return organizationRepository.findByCreatedBy(caller);
    }
    public Organization findById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organisation introuvable"));
    }
}