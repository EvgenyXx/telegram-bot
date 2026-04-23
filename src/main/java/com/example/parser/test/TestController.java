package com.example.parser.test;

import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final PlayerRepository playerRepository;

    @GetMapping("/test")
    public List<String> test() {
        return playerRepository.findAll()
                .stream()
                .map(Player::getName)
                .toList();
    }
}