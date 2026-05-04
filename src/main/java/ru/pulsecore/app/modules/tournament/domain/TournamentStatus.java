package ru.pulsecore.app.modules.tournament.domain;

public enum TournamentStatus {
    NOT_STARTED,
    STARTED,
    IN_PROGRESS,
    FINISHED,
    CANCELLED;

    public boolean isFinished() {
        return this == FINISHED;
    }
}