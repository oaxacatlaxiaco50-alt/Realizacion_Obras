package com.obraspublicas.features.obras.infrastructure.repository;

import com.obraspublicas.features.obras.domain.model.ObraMeta;
import com.obraspublicas.features.obras.domain.repository.ObraMetaRepository;
import com.obraspublicas.features.obras.infrastructure.entity.ObraMetaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ObraMetaRepositoryImpl implements ObraMetaRepository {

    private final ObraMetaJpaRepository jpa;

    @Override
    public ObraMeta save(ObraMeta meta) {
        ObraMetaEntity entity = toEntity(meta);
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<ObraMeta> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<ObraMeta> findByObraId(Long obraId) {
        return jpa.findByObraId(obraId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    private ObraMeta toDomain(ObraMetaEntity e) {
        return ObraMeta.builder()
                .id(e.getId())
                .obraId(e.getObraId())
                .concepto(e.getConcepto())
                .unidadMedida(e.getUnidadMedida())
                .cantidadMeta(e.getCantidadMeta())
                .avanceAcumulado(e.getAvanceAcumulado())
                .porcentaje(e.getPorcentaje())
                .estado(e.getEstado())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private ObraMetaEntity toEntity(ObraMeta d) {
        ObraMetaEntity e = ObraMetaEntity.builder()
                .id(d.getId())
                .obraId(d.getObraId())
                .concepto(d.getConcepto())
                .unidadMedida(d.getUnidadMedida())
                .cantidadMeta(d.getCantidadMeta())
                .avanceAcumulado(d.getAvanceAcumulado() != null ? d.getAvanceAcumulado() : 0.0)
                .porcentaje(d.getPorcentaje() != null ? d.getPorcentaje() : 0)
                .estado(d.getEstado())
                .build();
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }
}
