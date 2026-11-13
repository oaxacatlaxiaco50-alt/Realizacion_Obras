package com.obraspublicas.features.configuracion.infrastructure.repository;

import com.obraspublicas.features.configuracion.domain.model.InstitucionConfig;
import com.obraspublicas.features.configuracion.domain.repository.InstitucionConfigRepository;
import com.obraspublicas.features.configuracion.infrastructure.entity.InstitucionConfigEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InstitucionConfigRepositoryImpl implements InstitucionConfigRepository {

    private final InstitucionConfigJpaRepository jpaRepository;

    @Override
    public Optional<InstitucionConfig> get() {
        return jpaRepository.findById(1L).map(this::toDomain);
    }

    @Override
    public InstitucionConfig save(InstitucionConfig config) {
        InstitucionConfigEntity entity = toEntity(config);
        entity.setId(1L);
        entity.setUpdatedAt(LocalDateTime.now());
        return toDomain(jpaRepository.save(entity));
    }

    private InstitucionConfig toDomain(InstitucionConfigEntity entity) {
        return InstitucionConfig.builder()
                .id(entity.getId())
                .nombreMunicipio(entity.getNombreMunicipio())
                .lemaOficial(entity.getLemaOficial())
                .periodoAdministracion(entity.getPeriodoAdministracion())
                .dependencia(entity.getDependencia())
                .logoUrl(entity.getLogoUrl())
                .membreteUrl(entity.getMembreteUrl())
                .piePaginaReporte(entity.getPiePaginaReporte())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private InstitucionConfigEntity toEntity(InstitucionConfig domain) {
        return InstitucionConfigEntity.builder()
                .id(domain.getId() != null ? domain.getId() : 1L)
                .nombreMunicipio(domain.getNombreMunicipio())
                .lemaOficial(domain.getLemaOficial())
                .periodoAdministracion(domain.getPeriodoAdministracion())
                .dependencia(domain.getDependencia())
                .logoUrl(domain.getLogoUrl())
                .membreteUrl(domain.getMembreteUrl())
                .piePaginaReporte(domain.getPiePaginaReporte())
                .updatedAt(domain.getUpdatedAt() != null ? domain.getUpdatedAt() : LocalDateTime.now())
                .build();
    }
}
