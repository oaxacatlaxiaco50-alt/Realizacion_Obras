package com.obraspublicas.features.audit.domain.repository;

import com.obraspublicas.features.audit.domain.model.Bitacora;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BitacoraRepository {
    Bitacora save(Bitacora bitacora);
    Page<Bitacora> findAll(Pageable pageable);
    Page<Bitacora> findByObraId(Long obraId, Pageable pageable);
    Optional<Bitacora> findById(Long id);
}
