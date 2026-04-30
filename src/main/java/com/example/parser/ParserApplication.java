package com.example.parser;


import com.example.parser.modules.shared.AdminProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;


@SpringBootApplication
@EnableConfigurationProperties(AdminProperties.class)
@EnableScheduling
public class ParserApplication {

	public static void main(String[] args)  {

		System.out.println("🔥🔥🔥 VERSION = 2 🔥🔥🔥");
		SpringApplication.run(ParserApplication.class, args);

	}
	@PostConstruct
	void setTimezone() {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
	}
}