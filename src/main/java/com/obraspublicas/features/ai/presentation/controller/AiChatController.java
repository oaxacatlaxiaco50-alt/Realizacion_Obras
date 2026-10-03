package com.obraspublicas.features.ai.presentation.controller;

import com.obraspublicas.features.ai.application.service.AiAuditService;
import com.obraspublicas.features.ai.application.service.AiChatService;
import com.obraspublicas.features.ai.application.service.AiAuditService.AiAuditResult;
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
    private final AiAuditService aiAuditService;

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails != null ? userDetails.getUsername() : "Usuario";
        AiChatResponse response = aiChatService.processMessage(request, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/audit/avance/{avanceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AiAuditResult> auditAvance(@PathVariable Long avanceId) {
        AiAuditResult result = aiAuditService.auditAvance(avanceId);
        return ResponseEntity.ok(result);
    }
}
