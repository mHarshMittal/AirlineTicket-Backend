package com.skybooker.flight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FlightServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightServiceApplication.class, args);
	}

}
