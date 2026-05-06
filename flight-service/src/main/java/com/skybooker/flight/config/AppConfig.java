package com.skybooker.flight.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

// Creates RestTemplate bean for calling other microservices ( seat-service)
@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    } // HTTP client for inter-service communication
}
