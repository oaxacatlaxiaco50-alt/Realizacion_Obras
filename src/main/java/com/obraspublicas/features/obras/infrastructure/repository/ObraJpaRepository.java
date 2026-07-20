package com.obraspublicas.features.obras.infrastructure.repository;

import com.obraspublicas.features.obras.infrastructure.entity.ObraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObraJpaRepository extends JpaRepository<ObraEntity, Long>, JpaSpecificationExecutor<ObraEntity> {
    Optional<ObraEntity> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
