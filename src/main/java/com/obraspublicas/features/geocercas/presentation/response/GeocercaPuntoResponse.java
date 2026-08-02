package com.obraspublicas.features.geocercas.presentation.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeocercaPuntoResponse {
    private Long id;
    private Double latitud;
    private Double longitud;
    private Integer orden;
}
