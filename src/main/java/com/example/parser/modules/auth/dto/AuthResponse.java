package com.example.parser.modules.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String id;
    private String name;
    private String email;
}