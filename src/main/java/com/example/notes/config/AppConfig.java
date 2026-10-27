package com.example.notes.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /** Clock как бин — время можно подменять в тестах. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
