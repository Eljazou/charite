package com.example.charite.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProfileRequest {
    private String fullName;
    private String avatarUrl;
    private MultipartFile avatarFile;
    private String theme;
    private String language;
}