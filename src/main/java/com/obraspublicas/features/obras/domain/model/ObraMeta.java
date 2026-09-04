package com.obraspublicas.features.obras.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObraMeta {
    private Long id;
    private Long obraId;
    private String concepto;
    private String unidadMedida;
    private Double cantidadMeta;
    private Double avanceAcumulado;
    private Integer porcentaje;
    private ObraMetaEstado estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
