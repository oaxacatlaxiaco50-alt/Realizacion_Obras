package com.obraspublicas.features.audit.presentation.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private String username;
    private String action;
    private String module;
    private LocalDateTime timestamp;
    private String ip;
    private String previousData;
    private String newData;
}
