package com.obraspublicas.features.ai.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private String response;
    private String conversationId;
    private LocalDateTime timestamp;
    private List<String> suggestedFollowUps;
    private String modelName;
}
