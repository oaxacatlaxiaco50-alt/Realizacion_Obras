package com.obraspublicas.features.obras.domain.repository;

import com.obraspublicas.features.obras.domain.model.ObraMeta;

import java.util.List;
import java.util.Optional;

public interface ObraMetaRepository {
    ObraMeta save(ObraMeta meta);
    Optional<ObraMeta> findById(Long id);
    List<ObraMeta> findByObraId(Long obraId);
    void deleteById(Long id);
}
