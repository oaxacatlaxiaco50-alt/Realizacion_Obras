package com.obraspublicas.features.archivos.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObraArchivo {
    private Long id;
    private Long obraId;
    private CarpetaTipo carpeta;
    private String nombreOriginal;
    private String archivoUrl;
    private TipoArchivo tipoArchivo;
    private Long tamanioBytes;
    private Long subidoPor;
    private LocalDateTime createdAt;
}
