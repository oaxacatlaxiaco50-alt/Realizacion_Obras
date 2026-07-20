package com.obraspublicas.features.expedientes.infrastructure.repository;

import com.obraspublicas.features.expedientes.infrastructure.entity.CatalogoDocumentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogoDocumentoJpaRepository extends JpaRepository<CatalogoDocumentoEntity, Long> {
    List<CatalogoDocumentoEntity> findByActivoTrue();
}
