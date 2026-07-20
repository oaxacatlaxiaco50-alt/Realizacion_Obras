package com.obraspublicas.features.geocercas.infrastructure.repository;

import com.obraspublicas.features.geocercas.application.mapper.GeocercaMapper;
import com.obraspublicas.features.geocercas.domain.model.Geocerca;
import com.obraspublicas.features.geocercas.domain.repository.GeocercaRepository;
import com.obraspublicas.features.geocercas.infrastructure.entity.GeocercaEntity;
import com.obraspublicas.features.geocercas.infrastructure.entity.GeocercaPuntoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GeocercaRepositoryImpl implements GeocercaRepository {

    private final GeocercaJpaRepository jpaRepository;
    private final GeocercaMapper mapper;

    @Override
    public Geocerca save(Geocerca geocerca) {
        GeocercaEntity entity;

        if (geocerca.getId() != null) {
            entity = jpaRepository.findById(geocerca.getId())
                    .orElse(new GeocercaEntity());
        } else {
            entity = new GeocercaEntity();
        }

        entity.setObraId(geocerca.getObraId());
        entity.setNombre(geocerca.getNombre());
        entity.setDescripcion(geocerca.getDescripcion());

        // Sincronizar puntos manteniendo el orden
        entity.getPuntos().clear();
        if (geocerca.getPuntos() != null) {
            AtomicInteger idx = new AtomicInteger(0);
            List<GeocercaPuntoEntity> puntoEntities = geocerca.getPuntos().stream()
                    .map(p -> GeocercaPuntoEntity.builder()
                            .geocerca(entity)
                            .latitud(p.getLatitud())
                            .longitud(p.getLongitud())
                            .orden(idx.getAndIncrement())
                            .build())
                    .collect(Collectors.toList());
            entity.getPuntos().addAll(puntoEntities);
        }

        GeocercaEntity saved = jpaRepository.save(entity);
        return toDomainWithPuntos(saved);
    }

    @Override
    public Optional<Geocerca> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomainWithPuntos);
    }

    @Override
    public List<Geocerca> findByObraId(Long obraId) {
        return jpaRepository.findByObraId(obraId).stream()
                .map(this::toDomainWithPuntos)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Geocerca toDomainWithPuntos(GeocercaEntity entity) {
        Geocerca domain = mapper.toDomain(entity);
        domain.setPuntos(
                entity.getPuntos().stream()
                        .map(mapper::toDomain)
                        .collect(Collectors.toList())
        );
        return domain;
    }
}
