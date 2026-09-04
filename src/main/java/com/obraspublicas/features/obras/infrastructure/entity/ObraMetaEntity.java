package com.obraspublicas.features.obras.infrastructure.entity;

import com.obraspublicas.features.obras.domain.model.ObraMetaEstado;
import com.obraspublicas.shared.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "obra_metas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObraMetaEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    @Column(nullable = false, length = 255)
    private String concepto;

    @Column(name = "unidad_medida", nullable = false, length = 50)
    private String unidadMedida;

    @Column(name = "cantidad_meta", nullable = false)
    private Double cantidadMeta;

    @Column(name = "avance_acumulado", nullable = false)
    @Builder.Default
    private Double avanceAcumulado = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer porcentaje = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ObraMetaEstado estado = ObraMetaEstado.PENDIENTE;
}
