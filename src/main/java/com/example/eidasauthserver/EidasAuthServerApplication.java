package com.example.eidasauthserver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class EidasAuthServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EidasAuthServerApplication.class, args);
	}
}
