package com.obraspublicas.features.geocercas.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Geocerca {
    private Long id;
    private Long obraId;
    private String nombre;
    private String descripcion;
    private List<GeocercaPunto> puntos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
