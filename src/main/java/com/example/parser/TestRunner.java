package com.example.parser;

import com.example.parser.notification.TournamentDiscoveryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestRunner {

    @Bean
    CommandLineRunner run(TournamentDiscoveryService service) {
        return args -> {
            service.checkNewTournaments(459307336L);
        };
    }
}