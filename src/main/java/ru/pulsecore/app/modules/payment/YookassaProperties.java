package ru.pulsecore.app.modules.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.yookassa")
public class YookassaProperties {
    private int shopId;
    private String secretKey;
}