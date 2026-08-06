package com.obraspublicas.features.obras.presentation.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObraUpdateRequest {

    @Size(max = 255, message = "El nombre no debe superar los 255 caracteres")
    private String nombre;

    private String descripcion;

    private BigDecimal monto;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Long responsableId;

    @jakarta.validation.constraints.DecimalMin(value = "-90.0", message = "Latitud inválida")
    @jakarta.validation.constraints.DecimalMax(value = "90.0", message = "Latitud inválida")
    private Double latitud;

    @jakarta.validation.constraints.DecimalMin(value = "-180.0", message = "Longitud inválida")
    @jakarta.validation.constraints.DecimalMax(value = "180.0", message = "Longitud inválida")
    private Double longitud;

    @Size(max = 500, message = "La dirección no debe superar los 500 caracteres")
    private String direccion;

    private String categoria;
}
