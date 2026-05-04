package ru.pulsecore.app.core.dto;

import ru.pulsecore.app.modules.tournament.persistence.entity.TournamentLinkStatus;
import ru.pulsecore.app.modules.tournament.domain.ParsedResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TournamentLinkResult {

    private TournamentLinkStatus status;
    private ParsedResult parsed;

}