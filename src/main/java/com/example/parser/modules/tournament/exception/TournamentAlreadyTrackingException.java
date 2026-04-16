package com.example.parser.modules.tournament.exception;

import com.example.parser.modules.shared.exception.BusinessException;

public class TournamentAlreadyTrackingException extends BusinessException {

    //todo написать сообщение об ошибке

    public TournamentAlreadyTrackingException() {
        super("👀 Мы уже отслеживаем этот турнир\n⏳ Дождись его завершения");
    }
}
