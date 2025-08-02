package com.pathprep.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class GroqConfig {
    private final GroqProperties groqProperties;

    @Bean
    public WebClient groqWebClient() {
        return WebClient.builder()
                .baseUrl(groqProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + groqProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            // log request details here if needed
            return reactor.core.publisher.Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            // log response details here if needed
            return reactor.core.publisher.Mono.just(clientResponse);
        });
    }
}
