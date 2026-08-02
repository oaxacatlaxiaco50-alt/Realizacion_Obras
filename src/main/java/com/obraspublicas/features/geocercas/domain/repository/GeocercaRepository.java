package com.obraspublicas.features.geocercas.domain.repository;

import com.obraspublicas.features.geocercas.domain.model.Geocerca;

import java.util.List;
import java.util.Optional;

public interface GeocercaRepository {
    Geocerca save(Geocerca geocerca);
    Optional<Geocerca> findById(Long id);
    List<Geocerca> findByObraId(Long obraId);
    boolean existsById(Long id);
    void deleteById(Long id);
}
