package com.obraspublicas.features.avances.infrastructure.entity;

import com.obraspublicas.features.avances.domain.model.FaseEvidencia;
import com.obraspublicas.features.avances.domain.model.TipoEvidencia;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "avance_evidencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvanceEvidenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avance_id", nullable = false)
    private ObraAvanceEntity avance;

    @Column(name = "archivo_url", nullable = false, length = 500)
    private String archivoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoEvidencia tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FaseEvidencia fase;

    @Column(length = 255)
    private String descripcion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
