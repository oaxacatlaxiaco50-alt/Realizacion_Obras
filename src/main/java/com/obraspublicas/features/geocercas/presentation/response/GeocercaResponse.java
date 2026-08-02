package com.obraspublicas.features.geocercas.presentation.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeocercaResponse {
    private Long id;
    private Long obraId;
    private String nombre;
    private String descripcion;
    private List<GeocercaPuntoResponse> puntos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
