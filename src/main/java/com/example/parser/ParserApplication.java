package com.example.parser;

import com.example.parser.config.AdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication
@EnableConfigurationProperties(AdminProperties.class)
public class ParserApplication {

	public static void main(String[] args) throws Exception {

		SpringApplication.run(ParserApplication.class, args);

	}
}