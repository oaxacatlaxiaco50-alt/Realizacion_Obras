package com.obraspublicas.features.avances.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "obra_avances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObraAvanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(name = "fecha_avance", nullable = false)
    private LocalDate fechaAvance;

    @Column(nullable = false)
    private Integer porcentaje;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "registrado_por")
    private Long registradoPor;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "avance", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AvanceEvidenciaEntity> evidencias = new ArrayList<>();
}
