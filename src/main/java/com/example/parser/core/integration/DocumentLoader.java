package com.example.parser.core.integration;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentLoader {

    public Document load(String url) throws Exception {
        int attempts = 3;

        for (int i = 1; i <= attempts; i++) {
            try {
                return Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(60000)
                        .get();
            } catch (java.net.SocketTimeoutException e) {
                if (i == attempts) throw e;

                log.warn("⏱ retry {}/{} for {}", i, attempts, url);
                Thread.sleep(2000);
            }
        }

        throw new RuntimeException("Unreachable");
    }
}