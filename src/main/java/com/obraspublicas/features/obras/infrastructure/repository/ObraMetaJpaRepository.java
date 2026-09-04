package com.obraspublicas.features.obras.infrastructure.repository;

import com.obraspublicas.features.obras.infrastructure.entity.ObraMetaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObraMetaJpaRepository extends JpaRepository<ObraMetaEntity, Long> {
    List<ObraMetaEntity> findByObraId(Long obraId);
}
