package com.example.parser.modules.shared;


import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
@ConfigurationProperties(prefix = "app.masters-api")
public class MastersApiProperties {


    private String url;
    private String method;
    private String userAgent;
    private String action;
    private String country;
    private int timeout;

}
