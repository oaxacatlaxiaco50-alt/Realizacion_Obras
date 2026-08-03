package com.obraspublicas.features.archivos.domain.repository;

import com.obraspublicas.features.archivos.domain.model.CarpetaTipo;
import com.obraspublicas.features.archivos.domain.model.ObraArchivo;

import java.util.List;
import java.util.Optional;

public interface ObraArchivoRepository {
    ObraArchivo save(ObraArchivo archivo);
    Optional<ObraArchivo> findById(Long id);
    List<ObraArchivo> findByObraId(Long obraId);
    List<ObraArchivo> findByObraIdAndCarpeta(Long obraId, CarpetaTipo carpeta);
    void deleteById(Long id);
    boolean existsById(Long id);
}
