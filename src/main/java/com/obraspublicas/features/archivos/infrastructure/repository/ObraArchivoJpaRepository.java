package com.obraspublicas.features.archivos.infrastructure.repository;

import com.obraspublicas.features.archivos.domain.model.CarpetaTipo;
import com.obraspublicas.features.archivos.infrastructure.entity.ObraArchivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObraArchivoJpaRepository extends JpaRepository<ObraArchivoEntity, Long> {
    List<ObraArchivoEntity> findByObraId(Long obraId);
    List<ObraArchivoEntity> findByObraIdAndCarpeta(Long obraId, CarpetaTipo carpeta);
}
