package com.obraspublicas.features.geocercas.infrastructure.repository;

import com.obraspublicas.features.geocercas.infrastructure.entity.GeocercaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeocercaJpaRepository extends JpaRepository<GeocercaEntity, Long> {
    List<GeocercaEntity> findByObraId(Long obraId);
}
