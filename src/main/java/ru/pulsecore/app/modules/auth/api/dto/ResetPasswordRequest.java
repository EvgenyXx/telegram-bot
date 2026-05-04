package ru.pulsecore.app.modules.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String code;

    @NotBlank
    @Size(min = 4)
    private String password;
}