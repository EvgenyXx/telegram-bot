// TournamentApi.java
package ru.pulsecore.app.modules.tournament.api;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TournamentApi {
    public static final String BASE_PATH = "/api/tournament";
    public static final String ADD = "/add";
    public static final String SEARCH = "/search";

    public static final String PARAM_DATE = "date";
    public static final String SESSION_PLAYER_ID = "playerId";
    public static final String SESSION_PLAYER_NAME = "playerName";
    public static final String ADD_BATCH = "/add-batch";
    public static final String UPDATE_RESULT = "/result/{id}";
    public static final String PARAM_AMOUNT = "amount";
    public static final String PARAM_BONUS = "bonus";
    public static final String RESP_MESSAGE = "message";
    public static final String RESP_OK = "ok";
    public static final String SEARCH_WITH_STATUS = "/search-with-status";
}