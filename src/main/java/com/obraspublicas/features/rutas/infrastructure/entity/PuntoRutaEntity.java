package com.obraspublicas.features.rutas.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ruta_puntos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntoRutaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private RutaObraEntity ruta;

    @Column(nullable = false, precision = 10, scale = 7)
    private Double latitud;

    @Column(nullable = false, precision = 10, scale = 7)
    private Double longitud;

    /** Posición secuencial del punto dentro de la ruta (0-based). Define el camino real, no línea directa */
    @Column(nullable = false)
    private Integer orden;
}
