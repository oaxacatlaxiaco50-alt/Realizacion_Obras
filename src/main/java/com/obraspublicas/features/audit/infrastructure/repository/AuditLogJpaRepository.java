package com.obraspublicas.features.audit.infrastructure.repository;

import com.obraspublicas.features.audit.infrastructure.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {
    Page<AuditLogEntity> findByUsername(String username, Pageable pageable);
}
