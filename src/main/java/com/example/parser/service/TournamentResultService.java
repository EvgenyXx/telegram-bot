package com.example.parser.service;

import com.example.parser.entity.TournamentResultEntity;
import com.example.parser.repository.TournamentResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TournamentResultService {

    private final TournamentResultRepository repository;

    public void save(TournamentResultEntity entity) {
        repository.save(entity);
    }
}