package com.obraspublicas.features.geocercas.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "geocerca_puntos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeocercaPuntoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "geocerca_id", nullable = false)
    private GeocercaEntity geocerca;

    @Column(nullable = false, precision = 10, scale = 7)
    private Double latitud;

    @Column(nullable = false, precision = 10, scale = 7)
    private Double longitud;

    @Column(nullable = false)
    private Integer orden;
}
