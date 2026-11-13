package com.obraspublicas.features.configuracion.infrastructure.repository;

import com.obraspublicas.features.configuracion.infrastructure.entity.InstitucionConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitucionConfigJpaRepository extends JpaRepository<InstitucionConfigEntity, Long> {
}
