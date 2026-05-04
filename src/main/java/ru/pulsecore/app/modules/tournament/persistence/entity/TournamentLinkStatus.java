package ru.pulsecore.app.modules.tournament.persistence.entity;

public enum TournamentLinkStatus {

    ALREADY_TRACKED,     // уже есть в системе (главный твой кейс)
    USER_ALREADY_EXISTS, // уже есть у пользователя
    NOT_PARTICIPATING,   // юзер не участвует
    TRACKING_STARTED,    // начали отслеживание
    NOT_STARTED, FINISHED             // турнир завершён и добавлен
}