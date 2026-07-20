package com.obraspublicas.features.roles.domain.repository;

import com.obraspublicas.features.roles.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(String name);
    List<Role> findAll();
    Role save(Role role);
}
