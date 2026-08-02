package com.obraspublicas.features.rutas.presentation.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaObraResponse {
    private Long id;
    private Long obraId;
    private String nombre;
    private String descripcion;
    private List<PuntoRutaResponse> puntos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
