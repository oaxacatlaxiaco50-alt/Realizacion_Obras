package com.obraspublicas.features.notificaciones.presentation.controller;

import com.obraspublicas.features.notificaciones.application.service.NotificacionService;
import com.obraspublicas.features.notificaciones.presentation.response.NotificacionResponse;
import com.obraspublicas.features.users.infrastructure.entity.UserEntity;
import com.obraspublicas.features.users.infrastructure.repository.UserJpaRepository;
import com.obraspublicas.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de notificaciones del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UserJpaRepository userJpaRepository;

    // ─────────────────────────────────────────────────────
    // GET /notificaciones
    // Retorna las notificaciones paginadas del usuario actual
    // ─────────────────────────────────────────────────────
    @GetMapping
    @Operation(
        summary = "Listar notificaciones",
        description = "Retorna la lista paginada de notificaciones del usuario autenticado, ordenadas de más nueva a más antigua."
    )
    public ResponseEntity<Page<NotificacionResponse>> listar(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long usuarioId = getCurrentUserId();
        return ResponseEntity.ok(notificacionService.listar(usuarioId, pageable));
    }

    // ─────────────────────────────────────────────────────
    // GET /notificaciones/no-leidas/count
    // Retorna el conteo de notificaciones no leídas del usuario actual
    // ─────────────────────────────────────────────────────
    @GetMapping("/no-leidas/count")
    @Operation(
        summary = "Contar notificaciones no leídas",
        description = "Retorna el número total de notificaciones no leídas del usuario autenticado."
    )
    public ResponseEntity<Map<String, Long>> contarNoLeidas() {
        Long usuarioId = getCurrentUserId();
        long count = notificacionService.contarNoLeidas(usuarioId);
        return ResponseEntity.ok(Map.of("noLeidas", count));
    }

    // ─────────────────────────────────────────────────────
    // PATCH /notificaciones/{id}/leer
    // Marca una notificación específica como leída
    // ─────────────────────────────────────────────────────
    @PatchMapping("/{id}/leer")
    @Operation(
        summary = "Marcar notificación como leída",
        description = "Marca una notificación específica del usuario autenticado como leída."
    )
    public ResponseEntity<NotificacionResponse> marcarLeida(@PathVariable Long id) {
        Long usuarioId = getCurrentUserId();
        return ResponseEntity.ok(notificacionService.marcarLeida(id, usuarioId));
    }

    // ─────────────────────────────────────────────────────
    // PATCH /notificaciones/leer-todas
    // Marca todas las notificaciones del usuario actual como leídas
    // ─────────────────────────────────────────────────────
    @PatchMapping("/leer-todas")
    @Operation(
        summary = "Marcar todas como leídas",
        description = "Marca todas las notificaciones no leídas del usuario autenticado como leídas."
    )
    public ResponseEntity<Void> marcarTodasLeidas() {
        Long usuarioId = getCurrentUserId();
        notificacionService.marcarTodasLeidas(usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────
    // PRIVATE HELPER — obtiene el ID del usuario autenticado
    // a partir del username en el SecurityContextHolder
    // ─────────────────────────────────────────────────────
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userJpaRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
        return user.getId();
    }
}
