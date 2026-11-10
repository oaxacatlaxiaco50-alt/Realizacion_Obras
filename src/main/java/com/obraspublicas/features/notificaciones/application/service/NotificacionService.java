package com.obraspublicas.features.notificaciones.application.service;

import com.obraspublicas.features.notificaciones.domain.model.Notificacion;
import com.obraspublicas.features.notificaciones.domain.repository.NotificacionRepository;
import com.obraspublicas.features.notificaciones.presentation.response.NotificacionResponse;
import com.obraspublicas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository repository;

    // ─────────────────────────────────────────────────────
    // LIST NOTIFICATIONS (paginated, newest first)
    // ─────────────────────────────────────────────────────
    public Page<NotificacionResponse> listar(Long usuarioId, Pageable pageable) {
        log.info("Listando notificaciones para el usuario ID: {}", usuarioId);
        return repository.findByUsuarioId(usuarioId, pageable).map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────
    // COUNT UNREAD
    // ─────────────────────────────────────────────────────
    public long contarNoLeidas(Long usuarioId) {
        return repository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    // ─────────────────────────────────────────────────────
    // MARK ONE AS READ
    // ─────────────────────────────────────────────────────
    @Transactional
    public NotificacionResponse marcarLeida(Long id, Long usuarioId) {
        log.info("Marcando notificacion ID: {} como leída para usuario ID: {}", id, usuarioId);
        Notificacion n = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", "id", id));
        if (!Objects.equals(n.getUsuarioId(), usuarioId)) {
            throw new AccessDeniedException("No tiene permisos para modificar una notificación de otro usuario");
        }
        n.setLeida(true);
        return toResponse(repository.save(n));
    }

    // ─────────────────────────────────────────────────────
    // MARK ALL AS READ
    // ─────────────────────────────────────────────────────
    @Transactional
    public void marcarTodasLeidas(Long usuarioId) {
        log.info("Marcando todas las notificaciones como leídas para usuario ID: {}", usuarioId);
        repository.markAllAsReadByUsuarioId(usuarioId);
    }

    // ─────────────────────────────────────────────────────
    // CREATE NOTIFICATION (called internally by other services)
    // ─────────────────────────────────────────────────────
    @Transactional
    public void crearNotificacion(Long usuarioId, String titulo, String mensaje, String tipo, Long obraId, String obraNombre) {
        if (usuarioId == null) {
            return;
        }
        log.info("Creando notificacion '{}' para usuario ID: {}", titulo, usuarioId);
        Notificacion n = Notificacion.builder()
                .usuarioId(usuarioId)
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(tipo)
                .leida(false)
                .obraId(obraId)
                .obraNombre(obraNombre)
                .build();
        repository.save(n);
    }

    // ─────────────────────────────────────────────────────
    // PRIVATE MAPPER
    // ─────────────────────────────────────────────────────
    private NotificacionResponse toResponse(Notificacion n) {
        return NotificacionResponse.builder()
                .id(n.getId())
                .usuarioId(n.getUsuarioId())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .tipo(n.getTipo())
                .leida(n.isLeida())
                .obraId(n.getObraId())
                .obraNombre(n.getObraNombre())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
