package com.obraspublicas.features.expedientes.application.mapper;

import com.obraspublicas.features.expedientes.domain.model.CatalogoDocumento;
import com.obraspublicas.features.expedientes.domain.model.ExpedienteObra;
import com.obraspublicas.features.expedientes.infrastructure.entity.CatalogoDocumentoEntity;
import com.obraspublicas.features.expedientes.infrastructure.entity.ExpedienteObraEntity;
import org.springframework.stereotype.Component;

@Component
public class ExpedienteMapper {

    public CatalogoDocumento toDomain(CatalogoDocumentoEntity entity) {
        if (entity == null) return null;
        return CatalogoDocumento.builder()
                .id(entity.getId())
                .seccion(entity.getSeccion())
                .nombre(entity.getNombre())
                .requerido(entity.isRequerido())
                .activo(entity.isActivo())
                .build();
    }

    public ExpedienteObra toDomain(ExpedienteObraEntity entity) {
        if (entity == null) return null;
        return ExpedienteObra.builder()
                .id(entity.getId())
                .obraId(entity.getObraId())
                .documento(toDomain(entity.getDocumentoCatalogo()))
                .estado(entity.getEstado())
                .archivoUrl(entity.getArchivoUrl())
                .observaciones(entity.getObservaciones())
                .fechaRevision(entity.getFechaRevision())
                .revisadoPorId(entity.getRevisadoPorId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
