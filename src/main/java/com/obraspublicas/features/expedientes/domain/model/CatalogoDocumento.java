package com.obraspublicas.features.expedientes.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoDocumento {
    private Long id;
    private SeccionExpediente seccion;
    private String nombre;
    private boolean requerido;
    private boolean activo;
}
