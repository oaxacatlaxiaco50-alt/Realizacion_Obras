package com.obraspublicas.features.users.application.mapper;

import com.obraspublicas.features.roles.application.mapper.RoleMapper;
import com.obraspublicas.features.users.domain.model.User;
import com.obraspublicas.features.users.infrastructure.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    User toDomain(UserEntity entity);

    UserEntity toEntity(User domain);
}
