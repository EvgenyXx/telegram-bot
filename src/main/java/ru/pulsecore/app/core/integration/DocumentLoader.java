package ru.pulsecore.app.core.integration;

import ru.pulsecore.app.modules.shared.exception.SiteUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentLoader {

    private volatile long lastFailure = 0;
    private static final long COOLDOWN = 300_000; // 5 минут

    public Document load(String url) {
        if (System.currentTimeMillis() - lastFailure < COOLDOWN) {
            throw new SiteUnavailableException();
        }

        for (int i = 1; i <= 2; i++) {
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(10_000)
                        .get();
                lastFailure = 0;
                return doc;
            } catch (java.net.SocketTimeoutException e) {
                lastFailure = System.currentTimeMillis();
                if (i == 2) throw new SiteUnavailableException();
            } catch (Exception e) {
                throw new SiteUnavailableException();
            }
        }
        throw new SiteUnavailableException();
    }
}