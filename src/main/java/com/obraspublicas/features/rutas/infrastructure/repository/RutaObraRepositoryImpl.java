package com.obraspublicas.features.rutas.infrastructure.repository;

import com.obraspublicas.features.rutas.application.mapper.RutaObraMapper;
import com.obraspublicas.features.rutas.domain.model.PuntoRuta;
import com.obraspublicas.features.rutas.domain.model.RutaObra;
import com.obraspublicas.features.rutas.domain.repository.RutaObraRepository;
import com.obraspublicas.features.rutas.infrastructure.entity.PuntoRutaEntity;
import com.obraspublicas.features.rutas.infrastructure.entity.RutaObraEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RutaObraRepositoryImpl implements RutaObraRepository {

    private final RutaObraJpaRepository jpaRepository;
    private final RutaObraMapper mapper;

    @Override
    public RutaObra save(RutaObra ruta) {
        RutaObraEntity entity;

        if (ruta.getId() != null) {
            entity = jpaRepository.findById(ruta.getId())
                    .orElse(new RutaObraEntity());
        } else {
            entity = new RutaObraEntity();
        }

        entity.setObraId(ruta.getObraId());
        entity.setNombre(ruta.getNombre());
        entity.setDescripcion(ruta.getDescripcion());

        // Sincronizar puntos preservando el orden — esto es lo que da la ruta real (no línea directa)
        entity.getPuntos().clear();
        if (ruta.getPuntos() != null) {
            AtomicInteger idx = new AtomicInteger(0);
            List<PuntoRutaEntity> puntoEntities = ruta.getPuntos().stream()
                    .map(p -> PuntoRutaEntity.builder()
                            .ruta(entity)
                            .latitud(p.getLatitud())
                            .longitud(p.getLongitud())
                            .orden(idx.getAndIncrement())
                            .build())
                    .collect(Collectors.toList());
            entity.getPuntos().addAll(puntoEntities);
        }

        RutaObraEntity saved = jpaRepository.save(entity);
        return toDomainWithPuntos(saved);
    }

    @Override
    public Optional<RutaObra> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomainWithPuntos);
    }

    @Override
    public List<RutaObra> findByObraId(Long obraId) {
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

    private RutaObra toDomainWithPuntos(RutaObraEntity entity) {
        RutaObra domain = mapper.toDomain(entity);
        domain.setPuntos(
                entity.getPuntos().stream()
                        .map(mapper::toDomain)
                        .collect(Collectors.toList())
        );
        return domain;
    }
}
