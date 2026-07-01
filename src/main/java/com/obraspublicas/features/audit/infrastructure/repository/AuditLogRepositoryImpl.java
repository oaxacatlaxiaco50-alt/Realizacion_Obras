package com.obraspublicas.features.audit.infrastructure.repository;

import com.obraspublicas.features.audit.application.mapper.AuditMapper;
import com.obraspublicas.features.audit.domain.model.AuditLog;
import com.obraspublicas.features.audit.domain.repository.AuditLogRepository;
import com.obraspublicas.features.audit.infrastructure.entity.AuditLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;
    private final AuditMapper mapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogEntity entity = mapper.toEntity(auditLog);
        AuditLogEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Page<AuditLog> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<AuditLog> findByUsername(String username, Pageable pageable) {
        return jpaRepository.findByUsername(username, pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<AuditLog> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
