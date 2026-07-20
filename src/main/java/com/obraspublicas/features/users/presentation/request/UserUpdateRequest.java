package com.obraspublicas.features.users.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String firstName;

    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String lastName;

    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
    private String email;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    // Roles a reemplazar (opcional - si viene null no se modifican)
    private Set<String> roles;
}
