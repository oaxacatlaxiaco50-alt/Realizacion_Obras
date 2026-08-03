package com.obraspublicas.features.avances.domain.repository;

import com.obraspublicas.features.avances.domain.model.AvanceEvidencia;
import com.obraspublicas.features.avances.domain.model.ObraAvance;

import java.util.List;
import java.util.Optional;

public interface ObraAvanceRepository {
    ObraAvance save(ObraAvance avance);
    Optional<ObraAvance> findById(Long id);
    List<ObraAvance> findByObraIdChronological(Long obraId);
    Optional<ObraAvance> findUltimoAvance(Long obraId);
    void deleteById(Long id);
    void deleteEvidenciaById(Long id);
    AvanceEvidencia saveEvidencia(AvanceEvidencia evidencia);
}
