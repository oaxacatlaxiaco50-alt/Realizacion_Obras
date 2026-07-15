package com.obraspublicas.features.rutas.infrastructure.repository;

import com.obraspublicas.features.rutas.infrastructure.entity.RutaObraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RutaObraJpaRepository extends JpaRepository<RutaObraEntity, Long> {
    List<RutaObraEntity> findByObraId(Long obraId);
}
