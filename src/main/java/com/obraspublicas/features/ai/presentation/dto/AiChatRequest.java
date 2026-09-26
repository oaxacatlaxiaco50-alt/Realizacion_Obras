package com.obraspublicas.features.ai.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {
    @NotBlank(message = "El mensaje no puede estar vacío")
    private String message;
    
    private String conversationId;
}
