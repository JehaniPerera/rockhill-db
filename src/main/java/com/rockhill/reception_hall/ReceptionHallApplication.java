package com.rockhill.reception_hall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReceptionHallApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReceptionHallApplication.class, args);
	}
}