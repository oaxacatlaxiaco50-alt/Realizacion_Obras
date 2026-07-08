package com.obraspublicas.features.expedientes.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpedienteObra {
    private Long id;
    private Long obraId;
    
    // We include the full Catalog object to have the name and section available in the domain
    private CatalogoDocumento documento; 
    
    private EstadoDocumento estado;
    private String archivoUrl;
    private String observaciones;
    private LocalDateTime fechaRevision;
    private Long revisadoPorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
