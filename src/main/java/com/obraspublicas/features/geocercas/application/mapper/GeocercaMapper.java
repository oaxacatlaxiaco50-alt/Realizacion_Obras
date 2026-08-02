package com.obraspublicas.features.geocercas.application.mapper;

import com.obraspublicas.features.geocercas.domain.model.Geocerca;
import com.obraspublicas.features.geocercas.domain.model.GeocercaPunto;
import com.obraspublicas.features.geocercas.infrastructure.entity.GeocercaEntity;
import com.obraspublicas.features.geocercas.infrastructure.entity.GeocercaPuntoEntity;
import com.obraspublicas.features.geocercas.presentation.response.GeocercaResponse;
import com.obraspublicas.features.geocercas.presentation.response.GeocercaPuntoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GeocercaMapper {

    @Mapping(target = "obraId", source = "obra.id")
    Geocerca toDomain(GeocercaEntity entity);

    @Mapping(target = "geocercaId", source = "geocerca.id")
    GeocercaPunto toDomain(GeocercaPuntoEntity entity);

    @Mapping(target = "obra.id", source = "obraId")
    @Mapping(target = "puntos", ignore = true)
    GeocercaEntity toEntity(Geocerca domain);

    @Mapping(target = "geocerca.id", source = "geocercaId")
    GeocercaPuntoEntity toEntity(GeocercaPunto domain);

    GeocercaResponse toResponse(Geocerca domain);

    GeocercaPuntoResponse toResponse(GeocercaPunto domain);
}
