package com.example.parser.modules.player.api;

public final class PlayerApi {
    public static final String SUBSCRIBE = "/{id}/subscribe";
    public static final String SEARCH = "/search";

    public static final String SEARCH_PARAM = "q";
    public static final String SUBSCRIPTION = "/{id}/subscription";
    public static final String DELETE_ACCOUNT = "/{id}";
    public static final String PAY = "/{id}/pay";

    private PlayerApi() {}

    public static final String BASE_PATH = "/api/player";
    public static final String DASHBOARD = "/{id}/dashboard";
    public static final String SUM = "/{id}/sum";
    public static final String TOURNAMENTS = "/{id}/tournaments";
    public static final String PROFILE = "/{id}/profile";
    public static final String CHANGE_PASSWORD = "/{id}/change-password";
}