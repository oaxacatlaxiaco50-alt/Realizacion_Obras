package com.obraspublicas.features.expedientes.infrastructure.repository;

import com.obraspublicas.features.expedientes.infrastructure.entity.ExpedienteObraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpedienteObraJpaRepository extends JpaRepository<ExpedienteObraEntity, Long> {
    List<ExpedienteObraEntity> findByObraId(Long obraId);
    boolean existsByObraId(Long obraId);
}
