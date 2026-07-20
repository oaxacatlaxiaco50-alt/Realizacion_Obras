package com.obraspublicas.features.auth.presentation.response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private Set<String> roles;
    private Set<String> permissions;
}
