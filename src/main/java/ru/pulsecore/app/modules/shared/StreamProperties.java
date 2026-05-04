package ru.pulsecore.app.modules.shared;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.stream")
public class StreamProperties {

    private List<Hall> halls;

    @Data
    public static class Hall {
        private String name;
        private String url;
    }
}