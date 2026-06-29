package com.obraspublicas.features.permissions.domain.repository;

import com.obraspublicas.features.permissions.domain.model.Permission;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository {
    Optional<Permission> findByName(String name);
    List<Permission> findAll();
    Permission save(Permission permission);
}
