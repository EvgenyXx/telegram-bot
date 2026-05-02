package com.example.parser.modules.player.service.auth;

import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.exception.BadResetCodeException;
import com.example.parser.modules.player.repository.PlayerRepository;
import com.example.parser.modules.player.service.strategy.MailStrategyRegistry;
import com.example.parser.modules.player.service.strategy.MailTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class PlayerPasswordResetService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailStrategyRegistry mailStrategyRegistry;

    public record Pending(String email, String code) {}

    public Pending initiate(String email) {
        String normalizedEmail = email.toLowerCase().trim();
        String code = String.format("%06d", new SecureRandom().nextInt(999999));
        mailStrategyRegistry.send(MailTypes.PASSWORD_RESET, normalizedEmail, code);
        return new Pending(normalizedEmail, code);
    }

    @Transactional
    public void complete(String email, String code, String expectedCode, String newPassword) {
        if (!expectedCode.equals(code)) {
            throw new BadResetCodeException();
        }
        Player player = playerRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(BadResetCodeException::new);
        player.setPassword(passwordEncoder.encode(newPassword));
        playerRepository.save(player);
    }
}