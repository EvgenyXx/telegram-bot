package com.example.parser.modules.player.api.dto;

import lombok.Builder;

@Builder
public record SubscriptionStatusResponse(boolean active, String expiresAt, String startedAt) {}