package com.obraspublicas.features.geocercas.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeocercaUpdateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no debe superar los 255 caracteres")
    private String nombre;

    private String descripcion;

    @NotNull(message = "Los puntos son obligatorios")
    @Size(min = 3, message = "Una geocerca debe tener al menos 3 puntos para formar un polígono")
    @Valid
    private List<GeocercaPuntoRequest> puntos;
}
