package com.obraspublicas.features.rutas.application.mapper;

import com.obraspublicas.features.rutas.domain.model.PuntoRuta;
import com.obraspublicas.features.rutas.domain.model.RutaObra;
import com.obraspublicas.features.rutas.infrastructure.entity.PuntoRutaEntity;
import com.obraspublicas.features.rutas.infrastructure.entity.RutaObraEntity;
import com.obraspublicas.features.rutas.presentation.response.PuntoRutaResponse;
import com.obraspublicas.features.rutas.presentation.response.RutaObraResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RutaObraMapper {

    @Mapping(target = "obraId", source = "obraId")
    RutaObra toDomain(RutaObraEntity entity);

    @Mapping(target = "rutaId", source = "ruta.id")
    PuntoRuta toDomain(PuntoRutaEntity entity);

    @Mapping(target = "puntos", ignore = true)
    RutaObraEntity toEntity(RutaObra domain);

    @Mapping(target = "ruta.id", source = "rutaId")
    PuntoRutaEntity toEntity(PuntoRuta domain);

    RutaObraResponse toResponse(RutaObra domain);

    PuntoRutaResponse toResponse(PuntoRuta domain);
}
