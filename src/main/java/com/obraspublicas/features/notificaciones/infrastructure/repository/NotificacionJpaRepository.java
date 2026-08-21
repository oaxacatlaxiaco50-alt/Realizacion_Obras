package com.obraspublicas.features.notificaciones.infrastructure.repository;

import com.obraspublicas.features.notificaciones.infrastructure.entity.NotificacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionJpaRepository extends JpaRepository<NotificacionEntity, Long> {

    Page<NotificacionEntity> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId, Pageable pageable);

    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    @Modifying
    @Query("UPDATE NotificacionEntity n SET n.leida = true WHERE n.usuarioId = :usuarioId AND n.leida = false")
    void markAllAsReadByUsuarioId(Long usuarioId);
}
