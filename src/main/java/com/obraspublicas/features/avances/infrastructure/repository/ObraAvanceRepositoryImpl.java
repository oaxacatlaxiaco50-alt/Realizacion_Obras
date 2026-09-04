package com.obraspublicas.features.avances.infrastructure.repository;

import com.obraspublicas.features.avances.domain.model.AvanceEvidencia;
import com.obraspublicas.features.avances.domain.model.ObraAvance;
import com.obraspublicas.features.avances.domain.repository.ObraAvanceRepository;
import com.obraspublicas.features.avances.infrastructure.entity.AvanceEvidenciaEntity;
import com.obraspublicas.features.avances.infrastructure.entity.ObraAvanceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ObraAvanceRepositoryImpl implements ObraAvanceRepository {

    private final ObraAvanceJpaRepository jpa;
    private final AvanceEvidenciaJpaRepository evidenciaJpa;

    @Override
    public ObraAvance save(ObraAvance avance) {
        ObraAvanceEntity entity = toEntity(avance);
        if (avance.getId() != null) {
            ObraAvanceEntity existing = jpa.findById(avance.getId()).orElse(null);
            if (existing != null) {
                entity.setEvidencias(existing.getEvidencias());
            }
        }
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<ObraAvance> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<ObraAvance> findByObraIdChronological(Long obraId) {
        return jpa.findByObraIdOrderByFechaAvanceDescCreatedAtDesc(obraId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<ObraAvance> findUltimoAvance(Long obraId) {
        return jpa.findFirstByObraIdOrderByPorcentajeDesc(obraId).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
    
    @Override
    public void deleteEvidenciaById(Long id) {
        evidenciaJpa.deleteById(id);
    }
    
    @Override
    public AvanceEvidencia saveEvidencia(AvanceEvidencia evidencia) {
        ObraAvanceEntity avanceEntity = jpa.findById(evidencia.getAvanceId()).orElseThrow();
        AvanceEvidenciaEntity e = AvanceEvidenciaEntity.builder()
            .avance(avanceEntity)
            .archivoUrl(evidencia.getArchivoUrl())
            .tipo(evidencia.getTipo())
            .fase(evidencia.getFase())
            .descripcion(evidencia.getDescripcion())
            .build();
        return toDomain(evidenciaJpa.save(e));
    }

    private ObraAvance toDomain(ObraAvanceEntity e) {
        ObraAvance a = ObraAvance.builder()
                .id(e.getId())
                .obraId(e.getObraId())
                .titulo(e.getTitulo())
                .fechaAvance(e.getFechaAvance())
                .porcentaje(e.getPorcentaje())
                .observaciones(e.getObservaciones())
                .registradoPor(e.getRegistradoPor())
                .metaId(e.getMetaId())
                .cantidadEjecutada(e.getCantidadEjecutada())
                .acumuladoActual(e.getAcumuladoActual())
                .createdAt(e.getCreatedAt())
                .build();
        if (e.getEvidencias() != null) {
            a.setEvidencias(e.getEvidencias().stream().map(this::toDomain).collect(Collectors.toList()));
        }
        return a;
    }

    private AvanceEvidencia toDomain(AvanceEvidenciaEntity e) {
        return AvanceEvidencia.builder()
                .id(e.getId())
                .avanceId(e.getAvance().getId())
                .archivoUrl(e.getArchivoUrl())
                .tipo(e.getTipo())
                .fase(e.getFase())
                .descripcion(e.getDescripcion())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private ObraAvanceEntity toEntity(ObraAvance d) {
        return ObraAvanceEntity.builder()
                .id(d.getId())
                .obraId(d.getObraId())
                .titulo(d.getTitulo())
                .fechaAvance(d.getFechaAvance())
                .porcentaje(d.getPorcentaje())
                .observaciones(d.getObservaciones())
                .registradoPor(d.getRegistradoPor())
                .metaId(d.getMetaId())
                .cantidadEjecutada(d.getCantidadEjecutada())
                .acumuladoActual(d.getAcumuladoActual())
                .build();
    }
}
