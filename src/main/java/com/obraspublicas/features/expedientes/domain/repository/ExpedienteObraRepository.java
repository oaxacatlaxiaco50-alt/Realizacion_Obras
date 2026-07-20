package com.obraspublicas.features.expedientes.domain.repository;

import com.obraspublicas.features.expedientes.domain.model.ExpedienteObra;
import java.util.List;
import java.util.Optional;

public interface ExpedienteObraRepository {
    List<ExpedienteObra> saveAll(List<ExpedienteObra> expedientes);
    ExpedienteObra save(ExpedienteObra expediente);
    List<ExpedienteObra> findByObraId(Long obraId);
    Optional<ExpedienteObra> findById(Long id);
    boolean existsByObraId(Long obraId);
}
