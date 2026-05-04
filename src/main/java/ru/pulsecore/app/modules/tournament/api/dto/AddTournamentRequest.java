package ru.pulsecore.app.modules.tournament.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTournamentRequest {
    private String url;
}