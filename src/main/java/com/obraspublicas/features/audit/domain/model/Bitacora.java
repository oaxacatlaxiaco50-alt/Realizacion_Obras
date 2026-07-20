package com.obraspublicas.features.audit.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bitacora {
    private Long id;
    private Long obraId;
    private String description;
    private LocalDateTime timestamp;
    private Long userId;
    private String status;
}
