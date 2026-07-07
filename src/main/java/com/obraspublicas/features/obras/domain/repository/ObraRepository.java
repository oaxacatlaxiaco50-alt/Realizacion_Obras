package com.obraspublicas.features.obras.domain.repository;

import com.obraspublicas.features.obras.domain.model.Obra;
import com.obraspublicas.features.obras.domain.model.ObraEstatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface ObraRepository {
    Obra save(Obra obra);
    Optional<Obra> findById(Long id);
    Optional<Obra> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    Page<Obra> findAll(
            String codigo,
            String nombre,
            ObraEstatus estatus,
            Long responsableId,
            LocalDate startFechaInicio,
            LocalDate endFechaInicio,
            Pageable pageable
    );
    
    Page<Obra> searchGlobal(String keyword, Pageable pageable);
}
