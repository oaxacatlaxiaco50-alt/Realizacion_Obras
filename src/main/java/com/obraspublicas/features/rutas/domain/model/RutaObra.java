package com.obraspublicas.features.rutas.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RutaObra {
    private Long id;
    private Long obraId;
    private String nombre;
    private String descripcion;
    /** Puntos ordenados por 'orden' — representan el camino real trazado en el mapa, NO línea directa */
    private List<PuntoRuta> puntos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
