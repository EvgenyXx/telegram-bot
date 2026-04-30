package com.example.parser.modules.player.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionInfoDto {
    private boolean active;
    private String expiresAt;
}