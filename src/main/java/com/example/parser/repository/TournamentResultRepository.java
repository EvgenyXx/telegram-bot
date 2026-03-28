package com.example.parser.repository;

import com.example.parser.entity.TournamentResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentResultRepository extends JpaRepository<TournamentResultEntity, Long> {
}