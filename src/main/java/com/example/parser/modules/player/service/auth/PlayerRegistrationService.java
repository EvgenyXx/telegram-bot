package com.example.parser.modules.player.service.auth;

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

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlayerRegistrationService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailStrategyRegistry mailStrategyRegistry;
    private final SubscriptionRepository subscriptionRepository;
    private final TournamentAutoAddService tournamentAutoAddService;

    public record Pending(String name, String email, String password, String code) {}

    public Pending initiate(String name, String email, String rawPassword) {
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

        return new Pending(normalizedName, normalizedEmail, encodedPassword, code);
    }

    @Transactional
    public Player complete(Pending pending, String code) {
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

        return player;
    }
}