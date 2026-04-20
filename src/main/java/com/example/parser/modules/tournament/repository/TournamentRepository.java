package com.example.parser.modules.tournament.repository;



import com.example.parser.modules.tournament.domain.TournamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TournamentRepository extends JpaRepository<TournamentEntity, Long> {

    Optional<TournamentEntity> findByExternalId(Long externalId);

    Optional<TournamentEntity>findByLink(String link);


}