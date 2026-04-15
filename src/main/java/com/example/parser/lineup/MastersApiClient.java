package com.example.parser.lineup;

import com.example.parser.config.MastersApiProperties;
import com.example.parser.domain.dto.TournamentDto;
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

    public List<TournamentDto> loadTournaments() {
        try {
            log.info("🚀 Loading tournaments from API...");

            Connection connection = Jsoup.connect(properties.getUrl())
                    .method(Connection.Method.valueOf(properties.getMethod()))
                    .header("User-Agent", properties.getUserAgent())
                    .ignoreContentType(true)
                    .timeout(properties.getTimeout());

            // параметры запроса
            connection.data("action", properties.getAction());
            connection.data("country", properties.getCountry());

            Connection.Response res = connection.execute();

            String body = res.body();

            return mapper.readValue(
                    body,
                    new TypeReference<>() {
                    }
            );

        } catch (Exception e) {
            log.error("❌ API error", e);
            return List.of(); // безопасно
        }
    }


    public List<TournamentDto> loadTournaments(String date) {
        try {
            log.info("🚀 Loading tournaments for date={}", date);

            Connection connection = Jsoup.connect(properties.getUrl())
                    .method(Connection.Method.valueOf(properties.getMethod()))
                    .header("User-Agent", properties.getUserAgent())
                    .ignoreContentType(true)
                    .timeout(properties.getTimeout());

            connection.data("action", properties.getAction());
            connection.data("country", properties.getCountry());

            if (date != null) {
                connection.data("date", date);
            }

            Connection.Response res = connection.execute();

            return mapper.readValue(
                    res.body(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("❌ API error", e);
            return List.of();
        }
    }
}