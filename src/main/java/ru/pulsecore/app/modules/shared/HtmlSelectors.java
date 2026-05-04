package ru.pulsecore.app.modules.shared;

public final class HtmlSelectors {

    private HtmlSelectors() {}

    // ===== ОСНОВА =====
    public static final String ROW = ".ml_tour_game_list_row";

    /**
     * @deprecated использовать DOM-селекторы (STAGE, PLAYER, SCORE)
     */
    @Deprecated
    public static final String COL = ".ml_tour_game_list_col";

    // ===== ЭЛЕМЕНТЫ (НОВЫЙ ПОДХОД) =====
    public static final String STAGE = ".ml_tour_game_group";
    public static final String STATUS = ".ml_tour_game_status";
    public static final String PLAYER = ".ml_tour_game_plr";


    // ===== СТАТУСЫ (CLASS) =====
    public static final String STATUS_GOES_CLASS = "goes";
    public static final String STATUS_COMPLETED_CLASS = "completed";
    public static final String STATUS_REMOVED = "removed";

    // ===== СЕЛЕКТОРЫ =====
    public static final String STATUS_COMPLETED_SELECTOR = ".ml_tour_game_status.completed";

    public static final String SHORTLINK = "link[rel=shortlink]";
    public static final String DATE = "table.info_table tr:contains(Дата:) td";

    // ===== ИНФО ТУРНИРА =====

    public static final String TABLE = "table.info_table tr:contains(Зал) td";
    public static final String TIME_ROW_SELECTOR = "table.info_table tr";
    public static final String TIME_LABEL = "Время";
    public static final String TD_SELECTOR = "td";

    // ===== ИНДЕКСЫ КОЛОНОК (LEGACY) =====
    /**
     * @deprecated перейти на STAGE
     */
    @Deprecated
    public static final int COL_STAGE = 0;

    /**
     * @deprecated перейти на PLAYER
     */
    @Deprecated
    public static final int COL_PLAYER1 = 3;

    /**
     * @deprecated перейти на SCORE
     */
    @Deprecated
    public static final int COL_SCORE = 4;

    /**
     * @deprecated перейти на PLAYER
     */
    @Deprecated
    public static final int COL_PLAYER2 = 5;



    // ===== ALT (пока не трогаем) =====
    public static final String ROW_ALT = ".ml_tour_game_list_row";
    public static final String STATUS_ALT = ".ml_tour_game_status";
    public static final String PLAYER_ALT = ".ml_tour_game_plr";
    public static final String SCORE_ALT = ".ml_game_res_points";
    public static final String SETS_ALT = ".ml_game_res_sets";
}