package com.example.parser.app;


import com.example.parser.modules.shared.AdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;




@SpringBootApplication
@EnableConfigurationProperties(AdminProperties.class)
@EnableScheduling
public class ParserApplication {

	public static void main(String[] args)  {

		System.out.println("🔥🔥🔥 VERSION = 2 🔥🔥🔥");
		SpringApplication.run(ParserApplication.class, args);

	}
}