package com.obraspublicas.features.expedientes.infrastructure.entity;

import com.obraspublicas.features.expedientes.domain.model.EstadoDocumento;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "expedientes_obras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpedienteObraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "documento_catalogo_id", nullable = false)
    private CatalogoDocumentoEntity documentoCatalogo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoDocumento estado;

    @Column(name = "archivo_url")
    private String archivoUrl;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Column(name = "revisado_por")
    private Long revisadoPorId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
