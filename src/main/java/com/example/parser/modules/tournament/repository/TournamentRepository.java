package com.example.parser.modules.tournament.repository;



import com.example.parser.modules.tournament.domain.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    Optional<Tournament> findByExternalId(Long externalId);

    Optional<Tournament>findByLink(String link);


}