package com.obraspublicas.features.rutas.domain.repository;

import com.obraspublicas.features.rutas.domain.model.RutaObra;

import java.util.List;
import java.util.Optional;

public interface RutaObraRepository {
    RutaObra save(RutaObra ruta);
    Optional<RutaObra> findById(Long id);
    List<RutaObra> findByObraId(Long obraId);
    boolean existsById(Long id);
    void deleteById(Long id);
}
