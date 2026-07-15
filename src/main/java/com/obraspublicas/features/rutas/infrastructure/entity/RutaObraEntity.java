package com.obraspublicas.features.rutas.infrastructure.entity;

import com.obraspublicas.shared.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rutas_obras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaObraEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /** Lista de puntos ordenados por 'orden' — representa la ruta real trazada en el mapa */
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<PuntoRutaEntity> puntos = new ArrayList<>();
}
