package com.obraspublicas.features.obras.application.mapper;

import com.obraspublicas.features.obras.domain.model.Obra;
import com.obraspublicas.features.obras.infrastructure.entity.ObraEntity;
import com.obraspublicas.features.obras.presentation.response.ObraResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ObraMapper {

    Obra toDomain(ObraEntity entity);
    ObraEntity toEntity(Obra domain);
    ObraResponse toResponse(Obra domain);
}
