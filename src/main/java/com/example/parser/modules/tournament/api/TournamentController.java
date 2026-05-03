// TournamentController.java
package com.example.parser.modules.tournament.api;

import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.notification.service.TournamentProcessService;
import com.example.parser.modules.tournament.application.TournamentResultService;
import com.example.parser.modules.tournament.api.dto.AddTournamentRequest;
import com.example.parser.modules.tournament.api.dto.AddTournamentResponse;
import com.example.parser.modules.tournament.api.dto.TournamentSearchResult;
import com.example.parser.modules.tournament.service.TournamentSearchService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(TournamentApi.BASE_PATH)
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentProcessService tournamentProcessService;
    private final TournamentSearchService tournamentSearchService;
    private final TournamentResultService tournamentResultService;


    @PostMapping(TournamentApi.ADD)
    public ResponseEntity<AddTournamentResponse> addByUrl(@Valid @RequestBody AddTournamentRequest request, HttpSession session) {
        String playerId = (String) session.getAttribute(TournamentApi.SESSION_PLAYER_ID);
        var response = tournamentProcessService.processByUrl(request.getUrl(), playerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(TournamentApi.SEARCH)
    public ResponseEntity<List<TournamentDto>> searchTournaments(
            @RequestParam(TournamentApi.PARAM_DATE) String date,
            @RequestParam(required = false) String endDate,
            HttpSession session)  {

        String playerName = (String) session.getAttribute(TournamentApi.SESSION_PLAYER_NAME);
        if (playerName == null) {
            return ResponseEntity.status(401).build();
        }

        if (endDate != null && !endDate.isEmpty()) {
            return ResponseEntity.ok(tournamentSearchService.findByDateRangeAndPlayer(date, endDate, playerName));
        }
        return ResponseEntity.ok(tournamentSearchService.findByDateAndPlayer(date, playerName));
    }

    @PostMapping(TournamentApi.ADD_BATCH)
    public ResponseEntity<List<AddTournamentResponse>> addByUrls(@Valid @RequestBody List<AddTournamentRequest> requests, HttpSession session) {
        String playerId = (String) session.getAttribute(TournamentApi.SESSION_PLAYER_ID);
        List<String> urls = requests.stream().map(AddTournamentRequest::getUrl).toList();
        return ResponseEntity.ok(tournamentProcessService.processByUrls(urls, playerId));
    }

    @PutMapping(TournamentApi.UPDATE_RESULT)
    public ResponseEntity<Map<String, String>> updateResult(
            @PathVariable Long id,
            @RequestBody Map<String, Double> body,
            HttpSession session) {

        if (session.getAttribute(TournamentApi.SESSION_PLAYER_ID) == null) {
            return ResponseEntity.status(401).build();
        }

        tournamentResultService.updateResult(
                id,
                body.get(TournamentApi.PARAM_AMOUNT),
                body.get(TournamentApi.PARAM_BONUS)
        );

        return ResponseEntity.ok(Map.of(TournamentApi.RESP_MESSAGE, TournamentApi.RESP_OK));
    }

    @GetMapping(TournamentApi.SEARCH_WITH_STATUS)
    public ResponseEntity<List<TournamentSearchResult>> searchTournamentsWithStatus(
            @RequestParam(TournamentApi.PARAM_DATE) String date,
            @RequestParam(required = false) String endDate,
            HttpSession session)  {

        String playerId = (String) session.getAttribute(TournamentApi.SESSION_PLAYER_ID);
        if (playerId == null) return ResponseEntity.status(401).build();

        if (endDate != null && !endDate.isEmpty()) {
            return ResponseEntity.ok(tournamentSearchService.findByDateRangeAndPlayerWithStatus(date, endDate, playerId));
        }
        return ResponseEntity.ok(tournamentSearchService.findByDateAndPlayerWithStatus(date, playerId));
    }




}