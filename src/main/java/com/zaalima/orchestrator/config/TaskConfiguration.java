package com.zaalima.orchestrator.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TaskProperties.class)
public class TaskConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
