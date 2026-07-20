package com.obraspublicas.features.permissions.application.mapper;

import com.obraspublicas.features.permissions.domain.model.Permission;
import com.obraspublicas.features.permissions.infrastructure.entity.PermissionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    Permission toDomain(PermissionEntity entity);

    PermissionEntity toEntity(Permission domain);
}
