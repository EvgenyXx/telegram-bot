package com.example.parser.modules.auth.dto;

import java.time.LocalDateTime;

public record MeResponse(String id, String name, String email, LocalDateTime createdAt) {}