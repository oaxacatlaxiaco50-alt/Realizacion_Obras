package com.obraspublicas.features.users.domain.repository;

import com.obraspublicas.features.users.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    List<User> findAll();
    User save(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
