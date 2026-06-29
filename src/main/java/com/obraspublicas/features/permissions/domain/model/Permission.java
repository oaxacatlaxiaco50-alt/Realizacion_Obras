package com.obraspublicas.features.permissions.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    private Long id;
    private String name;
    private String description;
}
