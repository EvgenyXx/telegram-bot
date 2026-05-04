package ru.pulsecore.app.modules.shared;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.info")
public class InfoProperties {

    private String url;
}