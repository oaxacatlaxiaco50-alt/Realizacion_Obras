package com.obraspublicas.features.obras.infrastructure.entity;

import com.obraspublicas.features.obras.domain.model.ObraEstatus;
import com.obraspublicas.shared.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "obras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObraEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ObraEstatus estatus;

    @Column(name = "responsable_id", nullable = false)
    private Long responsableId;

    @Column(columnDefinition = "DOUBLE PRECISION")
    private Double latitud;

    @Column(columnDefinition = "DOUBLE PRECISION")
    private Double longitud;

    @Column(name = "direccion", length = 500)
    private String direccion;

    @Column(name = "categoria", length = 100)
    private String categoria;
}
