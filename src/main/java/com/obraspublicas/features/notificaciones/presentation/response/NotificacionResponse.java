package com.obraspublicas.features.notificaciones.presentation.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificacionResponse {
    private Long id;
    private Long usuarioId;
    private String titulo;
    private String mensaje;
    private String tipo;
    private boolean leida;
    private Long obraId;
    private String obraNombre;
    private LocalDateTime createdAt;
}
