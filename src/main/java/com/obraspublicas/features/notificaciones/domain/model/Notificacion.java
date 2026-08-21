package com.obraspublicas.features.notificaciones.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notificacion {
    private Long id;
    private Long usuarioId;
    private String titulo;
    private String mensaje;
    private String tipo; // "exito", "advertencia", "error", "info"
    private boolean leida;
    private Long obraId;
    private String obraNombre;
    private LocalDateTime createdAt;
}
