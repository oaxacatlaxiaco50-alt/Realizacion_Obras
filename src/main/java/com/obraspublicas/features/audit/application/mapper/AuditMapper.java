package com.obraspublicas.features.audit.application.mapper;

import com.obraspublicas.features.audit.domain.model.AuditLog;
import com.obraspublicas.features.audit.domain.model.Bitacora;
import com.obraspublicas.features.audit.infrastructure.entity.AuditLogEntity;
import com.obraspublicas.features.audit.infrastructure.entity.BitacoraEntity;
import com.obraspublicas.features.audit.presentation.response.AuditLogResponse;
import com.obraspublicas.features.audit.presentation.response.BitacoraResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    AuditLog toDomain(AuditLogEntity entity);
    AuditLogEntity toEntity(AuditLog domain);
    AuditLogResponse toResponse(AuditLog domain);

    Bitacora toDomain(BitacoraEntity entity);
    BitacoraEntity toEntity(Bitacora domain);
    BitacoraResponse toResponse(Bitacora domain);
}
