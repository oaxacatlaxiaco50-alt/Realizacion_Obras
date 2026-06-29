package com.obraspublicas.features.roles.domain.model;

import com.obraspublicas.features.permissions.domain.model.Permission;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
