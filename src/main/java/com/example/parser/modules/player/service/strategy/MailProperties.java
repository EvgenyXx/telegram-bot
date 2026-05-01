package com.example.parser.modules.player.service.strategy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {
    private String from;
    private Verification verification = new Verification();
    private Reset reset = new Reset();

    @Getter
    @Setter
    public static class Verification {
        private String subject;
        private String text;
    }

    @Getter
    @Setter
    public static class Reset {
        private String subject;
        private String text;
    }
}