package com.example.parser.modules.player.exception;

import com.example.parser.modules.shared.exception.BusinessException;

public class PlayerNameAlreadyExistsException  extends BusinessException {


    public PlayerNameAlreadyExistsException() {
        super("❌ Пользователь с таким именем уже существует.\nПожалуйста, введи другое имя:");
    }
}
