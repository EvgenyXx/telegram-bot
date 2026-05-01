package com.example.parser.modules.player.service;

import com.example.parser.modules.auth.dto.AuthResponse;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.domain.Subscription;
import com.example.parser.modules.player.exception.BadCredentialsException;
import com.example.parser.modules.player.exception.BadResetCodeException;
import com.example.parser.modules.player.exception.EmailAlreadyExistsException;
import com.example.parser.modules.player.exception.PlayerNameAlreadyExistsException;
import com.example.parser.modules.player.repository.PlayerRepository;
import com.example.parser.modules.player.repository.SubscriptionRepository;
import com.example.parser.modules.player.service.strategy.MailStrategyRegistry;
import com.example.parser.modules.player.service.strategy.MailTypes;
import com.example.parser.modules.tournament.service.TournamentAutoAddService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlayerAuthService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailStrategyRegistry mailStrategyRegistry;
    private final SubscriptionRepository subscriptionRepository;
    private final TournamentAutoAddService tournamentAutoAddService;

    public record PendingRegistration(String name, String email, String password, String code) {}
    public record PendingReset(String email, String code) {}

    public PendingRegistration initiateRegistration(String name, String email, String rawPassword) {
        String normalizedEmail = email.toLowerCase().trim();
        String normalizedName = name.toLowerCase().trim();

        if (playerRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }
        if (playerRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new PlayerNameAlreadyExistsException();
        }

        String code = String.format("%06d", new SecureRandom().nextInt(999999));
        String encodedPassword = passwordEncoder.encode(rawPassword);

        mailStrategyRegistry.send(MailTypes.VERIFICATION, normalizedEmail, code);

        return new PendingRegistration(normalizedName, normalizedEmail, encodedPassword, code);
    }

    @Transactional
    public AuthResponse completeRegistration(PendingRegistration pending, String code) {
        if (!pending.code().equals(code)) {
            throw new BadCredentialsException();
        }

        Player player = playerRepository.save(Player.builder()
                .name(pending.name())
                .email(pending.email())
                .password(pending.password())
                .verified(true)
                .createdAt(LocalDateTime.now())
                .build());

        Subscription trial = Subscription.builder().player(player).build();
        trial.activate(7);
        subscriptionRepository.save(trial);

        tournamentAutoAddService.addRecentTournamentsForPlayer(player, 30);

        return AuthResponse.builder()
                .id(player.getId().toString())
                .name(player.getName())
                .email(player.getEmail())
                .build();
    }

    public AuthResponse authenticate(String email, String rawPassword) {
        String normalizedEmail = email.toLowerCase().trim();
        Player player = playerRepository.findByEmail(normalizedEmail)
                .orElseThrow(BadCredentialsException::new);
        if (!passwordEncoder.matches(rawPassword, player.getPassword())) {
            throw new BadCredentialsException();
        }
        return AuthResponse.builder()
                .id(player.getId().toString())
                .name(player.getName())
                .email(player.getEmail())
                .build();
    }

    public PendingReset initiatePasswordReset(String email) {
        String normalizedEmail = email.toLowerCase().trim();

        String code = String.format("%06d", new SecureRandom().nextInt(999999));
        mailStrategyRegistry.send(MailTypes.PASSWORD_RESET, normalizedEmail, code);

        return new PendingReset(normalizedEmail, code);
    }

    @Transactional
    public void completePasswordReset(String email, String code, String expectedCode, String newPassword) {
        if (!expectedCode.equals(code)) {
            throw new BadResetCodeException();
        }

        Player player = playerRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(BadResetCodeException::new);

        player.setPassword(passwordEncoder.encode(newPassword));
        playerRepository.save(player);
    }
}