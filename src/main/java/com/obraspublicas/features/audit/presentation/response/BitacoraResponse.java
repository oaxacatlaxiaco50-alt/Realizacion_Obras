package com.obraspublicas.features.audit.presentation.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BitacoraResponse {
    private Long id;
    private Long obraId;
    private String description;
    private LocalDateTime timestamp;
    private Long userId;
    private String status;
}
