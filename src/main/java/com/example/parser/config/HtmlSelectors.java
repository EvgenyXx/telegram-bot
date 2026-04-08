package com.example.parser.config;

public final class HtmlSelectors {

    private HtmlSelectors() {
        // запрет на создание объекта
    }

    // ===== ОСНОВА =====
    public static final String ROW = ".ml_tour_game_list_row";
    public static final String COL = ".ml_tour_game_list_col";
    public static final String PLAYER = ".ml_tour_game_plr";
    public static final String STATUS = ".ml_tour_game_status";

    // ===== СТАТУСЫ (CLASS) =====
    public static final String STATUS_GOES_CLASS = "goes";
    public static final String STATUS_COMPLETED_CLASS = "completed";
    public static final String STATUS_REMOVED = "removed";

    // ===== СЕЛЕКТОРЫ =====
    public static final String STATUS_COMPLETED_SELECTOR = ".ml_tour_game_status.completed";
    public static final String SHORTLINK = "link[rel=shortlink]";
    public static final String DATE = "table.info_table tr:contains(Дата:) td";

    // ===== ИНФО ТУРНИРА =====
    public static final String LEAGUE = "table.info_table tr:contains(Лига) td";
    public static final String TABLE = "table.info_table tr:contains(Зал) td";

    public static final String TIME_ROW_SELECTOR = "table.info_table tr";
    public static final String TIME_LABEL = "Время";
    public static final String TD_SELECTOR = "td";

    // ===== ИНДЕКСЫ КОЛОНОК =====
    public static final int COL_STAGE = 0;
    public static final int COL_PLAYER1 = 3;
    public static final int COL_SCORE = 4;
    public static final int COL_PLAYER2 = 5;


    public static final String ALL_MATCHES = ".ml_tour_game_list_row";
//    public static  final String PLAYERS = ".ml_tour_game_plr ";
}