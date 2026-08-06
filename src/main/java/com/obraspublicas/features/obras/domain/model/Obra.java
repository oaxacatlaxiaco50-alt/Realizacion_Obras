package com.obraspublicas.features.obras.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Obra {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal monto;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private ObraEstatus estatus;
    private Long responsableId;
    private Double latitud;
    private Double longitud;
    private String direccion;
    private String categoria;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
