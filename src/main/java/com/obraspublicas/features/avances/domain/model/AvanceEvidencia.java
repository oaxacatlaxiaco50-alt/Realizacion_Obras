package com.obraspublicas.features.avances.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvanceEvidencia {
    private Long id;
    private Long avanceId;
    private String archivoUrl;
    private TipoEvidencia tipo;
    private FaseEvidencia fase;
    private String descripcion;
    private LocalDateTime createdAt;
}
