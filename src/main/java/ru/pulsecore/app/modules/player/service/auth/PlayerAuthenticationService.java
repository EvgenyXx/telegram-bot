package ru.pulsecore.app.modules.player.service.auth;

import ru.pulsecore.app.modules.player.domain.Player;
import ru.pulsecore.app.modules.player.exception.BadCredentialsException;
import ru.pulsecore.app.modules.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerAuthenticationService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    public Player authenticate(String email, String rawPassword) {
        String normalizedEmail = email.toLowerCase().trim();
        Player player = playerRepository.findByEmail(normalizedEmail)
                .orElseThrow(BadCredentialsException::new);
        if (!passwordEncoder.matches(rawPassword, player.getPassword())) {
            throw new BadCredentialsException();
        }
        return player;
    }
}