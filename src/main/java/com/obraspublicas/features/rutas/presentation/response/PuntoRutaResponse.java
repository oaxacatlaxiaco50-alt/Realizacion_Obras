package com.obraspublicas.features.rutas.presentation.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntoRutaResponse {
    private Long id;
    private Double latitud;
    private Double longitud;
    private Integer orden;
}
