package com.example.parser.service;

import com.example.parser.entity.Player;
import com.example.parser.entity.TournamentResultEntity;
import com.example.parser.repository.TournamentResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentResultService {

    private final TournamentResultRepository repository;

    public void save(TournamentResultEntity entity) {
        repository.save(entity);
    }

    public List<TournamentResultEntity> getResultsByPeriod(Player player,
                                                           LocalDate start,
                                                           LocalDate end) {
        return repository.findByPlayerAndDateBetweenOrderByDateAsc(player, start, end);
    }

    public int getSumByPeriod(Player player, LocalDate start, LocalDate end) {
        return repository.sumByPlayerAndPeriod(player, start, end);
    }
}