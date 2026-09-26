package com.obraspublicas.features.ai.presentation.controller;

import com.obraspublicas.features.ai.application.service.AiChatService;
import com.obraspublicas.features.ai.presentation.dto.AiChatRequest;
import com.obraspublicas.features.ai.presentation.dto.AiChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SUPERVISOR')")
    public ResponseEntity<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails != null ? userDetails.getUsername() : "Usuario";
        AiChatResponse response = aiChatService.processMessage(request, username);
        return ResponseEntity.ok(response);
    }
}
