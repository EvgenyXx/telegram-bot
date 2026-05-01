package com.example.parser.modules.player.service;

import com.example.parser.modules.auth.dto.AuthResponse;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.domain.Subscription;
import com.example.parser.modules.player.exception.BadCredentialsException;
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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlayerAuthService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailStrategyRegistry mailStrategyRegistry;
    private final SubscriptionRepository subscriptionRepository;
    private final TournamentAutoAddService tournamentAutoAddService;

    @Transactional
    public AuthResponse register(String name, String email, String rawPassword) {
        String normalizedEmail = email.toLowerCase().trim();
        String normalizedName = name.toLowerCase().trim();

        if (playerRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }
        if (playerRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new PlayerNameAlreadyExistsException();
        }

        String code = String.format("%06d", new java.util.Random().nextInt(999999));

        Player player = playerRepository.save(Player.builder()
                .name(normalizedName)
                .email(normalizedEmail)
                .password(passwordEncoder.encode(rawPassword))
                .verificationCode(code)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build());

        mailStrategyRegistry.send(MailTypes.VERIFICATION, player.getEmail(), code);

        // Триал-подписка
        Subscription trial = Subscription.builder().player(player).build();
        trial.activate(7);
        subscriptionRepository.save(trial);

        // Авто-добавление турниров за последние 30 дней
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
        if (!player.isVerified()) {
            throw new BadCredentialsException();
        }
        return AuthResponse.builder()
                .id(player.getId().toString())
                .name(player.getName())
                .email(player.getEmail())
                .build();
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        String normalizedEmail = email.toLowerCase().trim();
        Player player = playerRepository.findByEmail(normalizedEmail)
                .orElseThrow(BadCredentialsException::new);
        if (!code.equals(player.getVerificationCode())) {
            throw new BadCredentialsException();
        }
        player.setVerified(true);
        player.setVerificationCode(null);
        playerRepository.save(player);
    }
}