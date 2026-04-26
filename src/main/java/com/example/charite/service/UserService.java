package com.example.charite.service;

import com.example.charite.dto.PasswordRequest;
import com.example.charite.dto.ProfileRequest;
import com.example.charite.dto.RegisterRequest;
import com.example.charite.enums.Role;
import com.example.charite.entity.User;
import com.example.charite.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        User u = new User();
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.USER);

        return userRepository.save(u);
    }
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User introuvable"));
    }

    public void registerWithRole(RegisterRequest req, String role) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        User u = new User();
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.valueOf(role));
        userRepository.save(u);
    }

    public void update(Long id, String fullName, String email, String role) {
        User u = findById(id);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setRole(Role.valueOf(role));
        userRepository.save(u);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }


    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    private static final String UPLOAD_DIR = "uploads/avatars/";

    public void updateProfile(ProfileRequest req) {
        User user = getCurrentUser();

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName());
        }

        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            try {
                String originalFilename = req.getAvatarFile().getOriginalFilename();
                String filename = UUID.randomUUID() + "_" + originalFilename;
                Path uploadPath = Paths.get(UPLOAD_DIR);
                Files.createDirectories(uploadPath);
                Files.copy(req.getAvatarFile().getInputStream(),
                        uploadPath.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
                user.setAvatarUrl("/uploads/avatars/" + filename);
            } catch (IOException e) {
                throw new RuntimeException("Erreur upload photo : " + e.getMessage());
            }
        }

        if (req.getTheme() != null && !req.getTheme().isBlank()) {
            user.setTheme(req.getTheme());
        }

        if (req.getLanguage() != null && !req.getLanguage().isBlank()) {
            user.setLanguage(req.getLanguage());
        }

        userRepository.save(user);
    }

    public void changePassword(PasswordRequest req) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("Confirmation incorrecte");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }
}