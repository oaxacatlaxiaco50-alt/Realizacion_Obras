package com.obraspublicas.features.audit.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    private Long id;
    private String username;
    private String action;
    private String module;
    private LocalDateTime timestamp;
    private String ip;
    private String previousData;
    private String newData;
}
