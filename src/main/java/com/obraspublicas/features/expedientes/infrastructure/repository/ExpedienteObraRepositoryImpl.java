package com.obraspublicas.features.expedientes.infrastructure.repository;

import com.obraspublicas.features.expedientes.application.mapper.ExpedienteMapper;
import com.obraspublicas.features.expedientes.domain.model.ExpedienteObra;
import com.obraspublicas.features.expedientes.domain.repository.ExpedienteObraRepository;
import com.obraspublicas.features.expedientes.infrastructure.entity.CatalogoDocumentoEntity;
import com.obraspublicas.features.expedientes.infrastructure.entity.ExpedienteObraEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExpedienteObraRepositoryImpl implements ExpedienteObraRepository {

    private final ExpedienteObraJpaRepository jpaRepository;
    private final ExpedienteMapper mapper;
    private final CatalogoDocumentoJpaRepository catalogoJpaRepository;

    @Override
    public List<ExpedienteObra> saveAll(List<ExpedienteObra> expedientes) {
        List<ExpedienteObraEntity> entities = expedientes.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        return jpaRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ExpedienteObra save(ExpedienteObra expediente) {
        return mapper.toDomain(jpaRepository.save(toEntity(expediente)));
    }

    @Override
    public List<ExpedienteObra> findByObraId(Long obraId) {
        return jpaRepository.findByObraId(obraId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ExpedienteObra> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByObraId(Long obraId) {
        return jpaRepository.existsByObraId(obraId);
    }

    private ExpedienteObraEntity toEntity(ExpedienteObra domain) {
        CatalogoDocumentoEntity catalogoEntity = catalogoJpaRepository.findById(domain.getDocumento().getId())
                .orElseThrow(() -> new RuntimeException("Catalogo no encontrado"));
                
        return ExpedienteObraEntity.builder()
                .id(domain.getId())
                .obraId(domain.getObraId())
                .documentoCatalogo(catalogoEntity)
                .estado(domain.getEstado())
                .archivoUrl(domain.getArchivoUrl())
                .observaciones(domain.getObservaciones())
                .fechaRevision(domain.getFechaRevision())
                .revisadoPorId(domain.getRevisadoPorId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
