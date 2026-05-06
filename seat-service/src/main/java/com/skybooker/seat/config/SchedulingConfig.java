package com.skybooker.seat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {

}


// Enables scheduling in the application
// Allows methods with @Scheduled to run automatically
// No logic needed, just activates background tasks