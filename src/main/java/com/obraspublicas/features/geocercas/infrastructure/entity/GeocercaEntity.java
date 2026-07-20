package com.obraspublicas.features.geocercas.infrastructure.entity;

import com.obraspublicas.shared.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "geocercas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeocercaEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @OneToMany(mappedBy = "geocerca", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<GeocercaPuntoEntity> puntos = new ArrayList<>();
}
