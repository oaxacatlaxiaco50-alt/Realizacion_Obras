package com.obraspublicas.features.roles.application.mapper;

import com.obraspublicas.features.permissions.application.mapper.PermissionMapper;
import com.obraspublicas.features.roles.domain.model.Role;
import com.obraspublicas.features.roles.infrastructure.entity.RoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {

    Role toDomain(RoleEntity entity);

    RoleEntity toEntity(Role domain);
}
