package com.obraspublicas.features.configuracion.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitucionConfigRequest {

    @NotBlank(message = "El nombre del municipio es obligatorio")
    private String nombreMunicipio;

    private String lemaOficial;
    private String periodoAdministracion;
    private String dependencia;
    private String piePaginaReporte;
}
