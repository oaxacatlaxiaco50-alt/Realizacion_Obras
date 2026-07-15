package com.obraspublicas.features.geocercas.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeocercaPunto {
    private Long id;
    private Long geocercaId;
    private Double latitud;
    private Double longitud;
    private Integer orden;
}
