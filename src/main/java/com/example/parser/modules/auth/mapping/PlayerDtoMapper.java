package com.example.parser.modules.auth.mapping;

import com.example.parser.modules.auth.api.dto.AuthResponse;
import com.example.parser.modules.player.domain.Player;
import org.springframework.stereotype.Component;

@Component
public class PlayerDtoMapper {
    public AuthResponse toAuthResponse(Player player) {
        return AuthResponse.builder()
                .id(player.getId().toString())
                .name(player.getName())
                .email(player.getEmail())
                .build();
    }
}