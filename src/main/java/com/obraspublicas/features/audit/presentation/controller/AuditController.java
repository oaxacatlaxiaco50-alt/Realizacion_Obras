package com.obraspublicas.features.audit.presentation.controller;

import com.obraspublicas.features.audit.application.mapper.AuditMapper;
import com.obraspublicas.features.audit.domain.repository.AuditLogRepository;
import com.obraspublicas.features.audit.presentation.response.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Endpoints para la consulta de logs de auditoría general")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final AuditMapper mapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('AUDIT_VIEW')")
    @Operation(summary = "Consultar logs de auditoría", description = "Devuelve el listado paginado de logs de auditoría general. Permite filtrar opcionalmente por usuario.")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) String username,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable
    ) {
        Page<AuditLogResponse> response;
        if (username != null && !username.trim().isEmpty()) {
            response = auditLogRepository.findByUsername(username, pageable).map(mapper::toResponse);
        } else {
            response = auditLogRepository.findAll(pageable).map(mapper::toResponse);
        }
        return ResponseEntity.ok(response);
    }
}
