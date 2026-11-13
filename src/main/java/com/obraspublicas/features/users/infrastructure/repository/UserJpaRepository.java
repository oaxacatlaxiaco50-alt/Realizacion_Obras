package com.obraspublicas.features.users.infrastructure.repository;

import com.obraspublicas.features.users.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.activeSessionId = :sessionId WHERE u.username = :username")
    void updateActiveSessionId(@Param("username") String username, @Param("sessionId") String sessionId);

    @Query("SELECT u.activeSessionId FROM UserEntity u WHERE u.username = :username")
    String getActiveSessionIdByUsername(@Param("username") String username);
}
