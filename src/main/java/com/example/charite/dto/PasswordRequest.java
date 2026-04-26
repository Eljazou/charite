package com.example.charite.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRequest {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
