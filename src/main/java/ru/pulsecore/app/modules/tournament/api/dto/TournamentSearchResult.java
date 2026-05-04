package ru.pulsecore.app.modules.tournament.api.dto;

import ru.pulsecore.app.core.dto.TournamentDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TournamentSearchResult {
    private TournamentDto tournament;
    private boolean saved;
}