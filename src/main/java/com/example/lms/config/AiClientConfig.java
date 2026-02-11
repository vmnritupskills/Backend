package com.example.lms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiClientConfig {

    @Bean
    public WebClient aiWebClient() {
        return WebClient.builder()
                .baseUrl("http://35.154.181.95:8000")
                .build();
    }
}

