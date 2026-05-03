package com.example.parser.modules.auth.api.dto;

import java.time.LocalDateTime;

public record MeResponse(String id, String name, String email, LocalDateTime createdAt) {}