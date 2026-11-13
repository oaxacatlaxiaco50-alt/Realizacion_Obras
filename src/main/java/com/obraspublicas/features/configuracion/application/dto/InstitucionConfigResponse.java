package com.obraspublicas.features.configuracion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitucionConfigResponse {
    private Long id;
    private String nombreMunicipio;
    private String lemaOficial;
    private String periodoAdministracion;
    private String dependencia;
    private String logoUrl;
    private String membreteUrl;
    private String piePaginaReporte;
    private LocalDateTime updatedAt;
}
