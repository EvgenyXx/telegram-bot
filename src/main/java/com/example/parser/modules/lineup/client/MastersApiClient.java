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


    public List<TournamentDto> loadTournaments(String date) {
        int attempts = 0;

        while (attempts < 3) {
            try {
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
                        new TypeReference<>() {
                        }
                );

            } catch (java.net.SocketTimeoutException e) {
                attempts++;
                log.warn("⚠️ Masters API timeout, attempt {}", attempts);

            } catch (Exception e) {
                log.error("❌ API error", e);
                return List.of();
            }
        }

        log.warn("❌ Masters API недоступен после 3 попыток");
        return List.of();
    }
}