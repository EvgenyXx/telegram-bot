package com.example.parser.modules.auth;

public final class AuthApi {
    private AuthApi() {}

    public static final String BASE_PATH = "/api/auth";
    public static final String REGISTER = "/register";
    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String ME = "/me";
    public static final String VERIFY_PASSWORD = "/verify-password";
    public static final String VERIFY_EMAIL = "/verify-email";

    public static final String SESSION_ID = "playerId";
    public static final String SESSION_NAME = "playerName";
    public static final String SESSION_EMAIL = "playerEmail";

    public static final String RESP_ID = "id";
    public static final String RESP_NAME = "name";
    public static final String RESP_EMAIL = "email";
    public static final String RESP_CREATED_AT = "createdAt";
    public static final String RESP_MESSAGE = "message";



    public static final String OK = "ok";

    public static final String CODE_EXPIRED = "Код не найден или истек";

    public static final String FORGOT_PASSWORD = "/forgot-password";
    public static final String RESET_PASSWORD = "/reset-password";
}