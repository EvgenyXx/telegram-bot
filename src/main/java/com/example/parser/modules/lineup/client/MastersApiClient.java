package com.example.parser.modules.lineup.client;

import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.shared.MastersApiProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MastersApiClient {

    private final MastersApiProperties properties;
    private final ObjectMapper mapper;

    private volatile long lastFailure = 0;
    private static final long COOLDOWN = 300_000; // 5 минут
    public List<TournamentDto> loadTournaments(String date) {
        if (System.currentTimeMillis() - lastFailure < COOLDOWN) {
            return List.of();
        }

        for (int i = 1; i <= 2; i++) {
            try {
                Connection connection = Jsoup.connect(properties.getUrl())
                        .method(Connection.Method.valueOf(properties.getMethod()))
                        .header("User-Agent", properties.getUserAgent())
                        .ignoreContentType(true)
                        .timeout(10_000);

                connection.data("action", properties.getAction());
                connection.data("country", properties.getCountry());
                if (date != null) connection.data("date", date);

                Connection.Response res = connection.execute();
                lastFailure = 0;
                return mapper.readValue(res.body(), new TypeReference<>() {});

            } catch (java.net.SocketTimeoutException e) {
                lastFailure = System.currentTimeMillis();
                log.warn("Masters API timeout, attempt {}", i);
            } catch (Exception e) {
                log.error("API error", e);
                return List.of();
            }
        }
        log.warn("Masters API недоступен");
        return List.of();
    }
}