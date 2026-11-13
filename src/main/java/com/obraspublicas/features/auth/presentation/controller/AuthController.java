package com.obraspublicas.features.auth.presentation.controller;

import com.obraspublicas.features.auth.application.service.AuthService;
import com.obraspublicas.features.auth.presentation.request.LoginRequest;
import com.obraspublicas.features.auth.presentation.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para iniciar sesión y gestión de tokens JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un token JWT con roles y permisos")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-session")
    @Operation(summary = "Verificar estado de sesión", description = "Verifica si el token actual sigue activo y no ha sido desplazado por otro inicio de sesión")
    public ResponseEntity<Map<String, String>> checkSession() {
        return ResponseEntity.ok(Map.of("status", "ACTIVE"));
    }
}
