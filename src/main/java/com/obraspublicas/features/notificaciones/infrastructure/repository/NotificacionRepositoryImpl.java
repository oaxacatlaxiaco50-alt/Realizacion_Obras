package com.obraspublicas.features.notificaciones.infrastructure.repository;

import com.obraspublicas.features.notificaciones.domain.model.Notificacion;
import com.obraspublicas.features.notificaciones.domain.repository.NotificacionRepository;
import com.obraspublicas.features.notificaciones.infrastructure.entity.NotificacionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificacionRepositoryImpl implements NotificacionRepository {

    private final NotificacionJpaRepository jpaRepository;

    @Override
    public Notificacion save(Notificacion n) {
        NotificacionEntity entity = toEntity(n);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Page<Notificacion> findByUsuarioId(Long usuarioId, Pageable pageable) {
        return jpaRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId, pageable)
                .map(this::toDomain);
    }

    @Override
    public Optional<Notificacion> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public long countByUsuarioIdAndLeidaFalse(Long usuarioId) {
        return jpaRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Override
    @Transactional
    public void markAllAsReadByUsuarioId(Long usuarioId) {
        jpaRepository.markAllAsReadByUsuarioId(usuarioId);
    }

    // ─────────────────────────────────────────────────────
    // PRIVATE MAPPERS
    // ─────────────────────────────────────────────────────

    private Notificacion toDomain(NotificacionEntity e) {
        return Notificacion.builder()
                .id(e.getId())
                .usuarioId(e.getUsuarioId())
                .titulo(e.getTitulo())
                .mensaje(e.getMensaje())
                .tipo(e.getTipo())
                .leida(e.isLeida())
                .obraId(e.getObraId())
                .obraNombre(e.getObraNombre())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private NotificacionEntity toEntity(Notificacion n) {
        return NotificacionEntity.builder()
                .id(n.getId())
                .usuarioId(n.getUsuarioId())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .tipo(n.getTipo())
                .leida(n.isLeida())
                .obraId(n.getObraId())
                .obraNombre(n.getObraNombre())
                .build();
    }
}
