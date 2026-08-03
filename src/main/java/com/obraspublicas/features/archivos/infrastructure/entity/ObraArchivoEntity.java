package com.obraspublicas.features.archivos.infrastructure.entity;

import com.obraspublicas.features.archivos.domain.model.CarpetaTipo;
import com.obraspublicas.features.archivos.domain.model.TipoArchivo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "obra_archivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObraArchivoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CarpetaTipo carpeta;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "archivo_url", nullable = false, length = 500)
    private String archivoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_archivo", nullable = false, length = 20)
    private TipoArchivo tipoArchivo;

    @Column(name = "tamanio_bytes")
    private Long tamanioBytes;

    @Column(name = "subido_por")
    private Long subidoPor;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
