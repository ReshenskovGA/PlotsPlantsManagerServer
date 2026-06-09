package com.garden.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    // Явное создание бина для Jackson 2.x, так как Spring Boot 4.x
    // по умолчанию конфигурирует только Jackson 3.x (tools.jackson)
    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }
}