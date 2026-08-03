package com.obraspublicas.features.avances.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObraAvance {
    private Long id;
    private Long obraId;
    private String titulo;
    private LocalDate fechaAvance;
    private Integer porcentaje;
    private String observaciones;
    private Long registradoPor;
    private LocalDateTime createdAt;
    private List<AvanceEvidencia> evidencias;
}
