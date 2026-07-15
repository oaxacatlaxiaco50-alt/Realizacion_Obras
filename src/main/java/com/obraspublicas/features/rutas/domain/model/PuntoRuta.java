package com.obraspublicas.features.rutas.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntoRuta {
    private Long id;
    private Long rutaId;
    private Double latitud;
    private Double longitud;
    private Integer orden;
}
