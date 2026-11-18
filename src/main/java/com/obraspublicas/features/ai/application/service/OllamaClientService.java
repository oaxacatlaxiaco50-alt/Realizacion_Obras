package com.obraspublicas.features.ai.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class OllamaClientService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    @Value("${ai.ollama.enabled:true}")
    private boolean enabled;

    @Value("${ai.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${ai.ollama.model:gemma3:1b}")
    private String model;

    @Value("${ai.ollama.timeout-ms:15000}")
    private long timeoutMs;

    @Value("${ai.ollama.max-tokens:180}")
    private int maxTokens;

    public boolean isEnabled() {
        return enabled;
    }

    public String getModelName() {
        return model;
    }

    public record ChatTurn(String role, String content) {}

    public Optional<String> askGemma(String systemPrompt, List<ChatTurn> history, String userMessage) {
        if (!enabled) {
            return Optional.empty();
        }
        long start = System.currentTimeMillis();
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            if (history != null) {
                for (ChatTurn turn : history) {
                    messages.add(Map.of("role", turn.role(), "content", turn.content()));
                }
            }
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> options = new LinkedHashMap<>();
            options.put("temperature", 0.25);
            options.put("num_predict", maxTokens);
            options.put("num_ctx", 2048);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);
            payload.put("stream", false);
            payload.put("keep_alive", "30m");
            payload.put("options", options);
            payload.put("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/chat"))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long elapsed = System.currentTimeMillis() - start;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                String content = root.path("message").path("content").asText("").trim();
                if (!content.isEmpty()) {
                    log.info("Gemma ({}) respondió en {} ms", model, elapsed);
                    return Optional.of(content);
                }
            } else {
                log.warn("Ollama devolvió HTTP {} en {} ms: {}", response.statusCode(), elapsed, response.body());
            }
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("No se pudo obtener respuesta de Ollama/Gemma en {} ms ({}): {}", elapsed, baseUrl, e.getMessage());
        }
        return Optional.empty();
    }
}
