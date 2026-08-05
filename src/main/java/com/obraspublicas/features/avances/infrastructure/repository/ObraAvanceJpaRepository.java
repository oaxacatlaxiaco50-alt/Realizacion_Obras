package com.obraspublicas.features.avances.infrastructure.repository;

import com.obraspublicas.features.avances.infrastructure.entity.ObraAvanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ObraAvanceJpaRepository extends JpaRepository<ObraAvanceEntity, Long> {
    List<ObraAvanceEntity> findByObraIdOrderByFechaAvanceDescCreatedAtDesc(Long obraId);
    Optional<ObraAvanceEntity> findFirstByObraIdOrderByPorcentajeDesc(Long obraId);
}
