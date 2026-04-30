package com.example.parser.modules.tournament.controller;


import com.example.parser.modules.notification.service.TournamentProcessService;
import com.example.parser.modules.tournament.api.TournamentApi;
import com.example.parser.modules.tournament.dto.AddTournamentRequest;
import com.example.parser.modules.tournament.dto.AddTournamentResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(TournamentApi.BASE_PATH)
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentProcessService tournamentProcessService;

    @PostMapping(TournamentApi.ADD)
    public ResponseEntity<AddTournamentResponse> addByUrl(@Valid @RequestBody AddTournamentRequest request, HttpSession session) {
        String playerId = (String) session.getAttribute("playerId");
        var response = tournamentProcessService.processByUrl(request.getUrl(), playerId);
        return ResponseEntity.ok(response);
    }
}