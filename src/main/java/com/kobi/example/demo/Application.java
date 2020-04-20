package com.kobi.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Calendar;

@SpringBootApplication
public class Application {
	//To execute the application browse to localhost:8080/swagger-ui.html
	//Credentials: kobi\1234
	//todo investigate flyway logs
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
