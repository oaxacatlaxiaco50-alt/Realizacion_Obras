package com.obraspublicas.features.expedientes.presentation.dto;

import com.obraspublicas.features.expedientes.domain.model.EstadoDocumento;
import lombok.Data;

@Data
public class ExpedienteUpdateRequest {
    private EstadoDocumento estado;
    private String observaciones;
}
