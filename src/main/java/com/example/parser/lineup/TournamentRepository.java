package com.example.parser.lineup;



import com.example.parser.domain.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    Optional<Tournament> findByExternalId(Long externalId);


}