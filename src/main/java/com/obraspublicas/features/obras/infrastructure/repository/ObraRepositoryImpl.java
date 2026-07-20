package com.obraspublicas.features.obras.infrastructure.repository;

import com.obraspublicas.features.obras.application.mapper.ObraMapper;
import com.obraspublicas.features.obras.domain.model.Obra;
import com.obraspublicas.features.obras.domain.model.ObraEstatus;
import com.obraspublicas.features.obras.domain.repository.ObraRepository;
import com.obraspublicas.features.obras.infrastructure.entity.ObraEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ObraRepositoryImpl implements ObraRepository {

    private final ObraJpaRepository jpaRepository;
    private final ObraMapper mapper;

    @Override
    public Obra save(Obra obra) {
        ObraEntity entity = mapper.toEntity(obra);
        ObraEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Obra> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Obra> findByCodigo(String codigo) {
        return jpaRepository.findByCodigo(codigo).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return jpaRepository.existsByCodigo(codigo);
    }

    @Override
    public Page<Obra> findAll(
            String codigo,
            String nombre,
            ObraEstatus estatus,
            Long responsableId,
            LocalDate startFechaInicio,
            LocalDate endFechaInicio,
            Pageable pageable
    ) {
        Specification<ObraEntity> spec = Specification.where(null);

        if (codigo != null && !codigo.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("codigo")), "%" + codigo.toLowerCase() + "%"));
        }
        if (nombre != null && !nombre.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
        }
        if (estatus != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estatus"), estatus));
        }
        if (responsableId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("responsableId"), responsableId));
        }
        if (startFechaInicio != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaInicio"), startFechaInicio));
        }
        if (endFechaInicio != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaInicio"), endFechaInicio));
        }

        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Obra> searchGlobal(String keyword, Pageable pageable) {
        Specification<ObraEntity> spec = Specification.where(null);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String likePattern = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("codigo")), likePattern),
                    cb.like(cb.lower(root.get("nombre")), likePattern),
                    cb.like(cb.lower(root.get("descripcion")), likePattern)
            ));
        }

        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }
}
