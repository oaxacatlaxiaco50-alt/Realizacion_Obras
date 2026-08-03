package com.obraspublicas.features.avances.infrastructure.repository;

import com.obraspublicas.features.avances.infrastructure.entity.AvanceEvidenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvanceEvidenciaJpaRepository extends JpaRepository<AvanceEvidenciaEntity, Long> {
}
