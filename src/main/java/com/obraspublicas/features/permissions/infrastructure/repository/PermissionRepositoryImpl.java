package com.obraspublicas.features.permissions.infrastructure.repository;

import com.obraspublicas.features.permissions.application.mapper.PermissionMapper;
import com.obraspublicas.features.permissions.domain.model.Permission;
import com.obraspublicas.features.permissions.domain.repository.PermissionRepository;
import com.obraspublicas.features.permissions.infrastructure.entity.PermissionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionJpaRepository jpaRepository;
    private final PermissionMapper mapper;

    @Override
    public Optional<Permission> findByName(String name) {
        return jpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public List<Permission> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Permission save(Permission permission) {
        PermissionEntity entity = mapper.toEntity(permission);
        PermissionEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
