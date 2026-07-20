package com.obraspublicas.features.expedientes.infrastructure.entity;

import com.obraspublicas.features.expedientes.domain.model.SeccionExpediente;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "catalogo_documentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoDocumentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "seccion", nullable = false)
    private SeccionExpediente seccion;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "requerido", nullable = false)
    private boolean requerido;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}
