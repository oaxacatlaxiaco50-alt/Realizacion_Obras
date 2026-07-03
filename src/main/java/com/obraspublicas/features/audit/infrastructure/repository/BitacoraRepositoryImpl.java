package com.obraspublicas.features.audit.infrastructure.repository;

import com.obraspublicas.features.audit.application.mapper.AuditMapper;
import com.obraspublicas.features.audit.domain.model.Bitacora;
import com.obraspublicas.features.audit.domain.repository.BitacoraRepository;
import com.obraspublicas.features.audit.infrastructure.entity.BitacoraEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BitacoraRepositoryImpl implements BitacoraRepository {

    private final BitacoraJpaRepository jpaRepository;
    private final AuditMapper mapper;

    @Override
    public Bitacora save(Bitacora bitacora) {
        BitacoraEntity entity = mapper.toEntity(bitacora);
        BitacoraEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Page<Bitacora> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Bitacora> findByObraId(Long obraId, Pageable pageable) {
        return jpaRepository.findByObraId(obraId, pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<Bitacora> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
