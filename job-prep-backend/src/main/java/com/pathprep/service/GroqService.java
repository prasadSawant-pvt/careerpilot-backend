package com.pathprep.service;

import com.pathprep.config.GroqProperties;
import com.pathprep.model.GroqChatRequest;
import com.pathprep.model.GroqChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqService {
    private final WebClient groqWebClient;
    private final GroqProperties groqProperties;

    public Mono<String> generateText(String prompt) {
        GroqChatRequest request = GroqChatRequest.simple(prompt, groqProperties.getDefaultModel());
        return groqWebClient
                .post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GroqChatResponse.class)
                .map(resp -> resp.getChoices().get(0).getMessage().getContent())
                .doOnError(e -> log.error("Groq API error: {}", e.getMessage()));
    }
}
