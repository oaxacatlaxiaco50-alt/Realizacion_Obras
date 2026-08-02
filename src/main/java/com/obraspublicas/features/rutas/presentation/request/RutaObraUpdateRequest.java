package com.obraspublicas.features.rutas.presentation.request;

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
public class RutaObraUpdateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no debe superar los 255 caracteres")
    private String nombre;

    private String descripcion;

    @NotNull(message = "Los puntos son obligatorios")
    @Size(min = 2, message = "Una ruta debe tener al menos 2 puntos (origen y destino)")
    @Valid
    private List<PuntoRutaRequest> puntos;
}
