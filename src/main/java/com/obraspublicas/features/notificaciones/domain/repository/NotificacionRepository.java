package com.obraspublicas.features.notificaciones.domain.repository;

import com.obraspublicas.features.notificaciones.domain.model.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface NotificacionRepository {
    Notificacion save(Notificacion notificacion);
    Page<Notificacion> findByUsuarioId(Long usuarioId, Pageable pageable);
    Optional<Notificacion> findById(Long id);
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);
    void markAllAsReadByUsuarioId(Long usuarioId);
}
