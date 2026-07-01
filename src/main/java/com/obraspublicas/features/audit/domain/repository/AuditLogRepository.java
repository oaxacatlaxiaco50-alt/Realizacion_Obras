package com.obraspublicas.features.audit.domain.repository;

import com.obraspublicas.features.audit.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AuditLogRepository {
    AuditLog save(AuditLog auditLog);
    Page<AuditLog> findAll(Pageable pageable);
    Page<AuditLog> findByUsername(String username, Pageable pageable);
    Optional<AuditLog> findById(Long id);
}
