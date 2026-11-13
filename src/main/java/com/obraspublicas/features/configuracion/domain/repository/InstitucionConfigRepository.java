package com.obraspublicas.features.configuracion.domain.repository;

import com.obraspublicas.features.configuracion.domain.model.InstitucionConfig;

import java.util.Optional;

public interface InstitucionConfigRepository {
    Optional<InstitucionConfig> get();
    InstitucionConfig save(InstitucionConfig config);
}
