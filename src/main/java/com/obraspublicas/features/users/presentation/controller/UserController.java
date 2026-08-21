package com.obraspublicas.features.users.presentation.controller;

import com.obraspublicas.features.users.application.service.UserService;
import com.obraspublicas.features.users.presentation.request.UserCreateRequest;
import com.obraspublicas.features.users.presentation.request.UserUpdateRequest;
import com.obraspublicas.features.users.presentation.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // ─────────────────────────────────────────────────────
    // GET /users
    // ADMINISTRADOR → ve todos los usuarios del sistema
    // SUPERVISOR    → ve todos los usuarios del sistema
    // CONTRATISTA   → ❌ acceso denegado
    // AUDITOR       → ❌ acceso denegado
    // ─────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('USER_VIEW')")
    @Operation(
        summary = "Listar usuarios",
        description = "Retorna la lista completa de usuarios. Requiere permiso USER_VIEW (ADMINISTRADOR, SUPERVISOR)."
    )
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    // ─────────────────────────────────────────────────────
    // GET /users/{id}
    // ADMINISTRADOR → ve cualquier usuario
    // SUPERVISOR    → ve cualquier usuario
    // CONTRATISTA   → ❌ acceso denegado
    // AUDITOR       → ❌ acceso denegado
    // ─────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('USER_VIEW')")
    @Operation(
        summary = "Obtener usuario por ID",
        description = "Retorna el detalle de un usuario por su ID. Requiere permiso USER_VIEW."
    )
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // ─────────────────────────────────────────────────────
    // POST /users
    // ADMINISTRADOR → ✅ puede crear usuarios con cualquier rol
    // SUPERVISOR    → ❌ acceso denegado
    // CONTRATISTA   → ❌ acceso denegado
    // AUDITOR       → ❌ acceso denegado
    // ─────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('USER_CREATE')")
    @Operation(
        summary = "Crear usuario",
        description = "Crea un nuevo usuario en el sistema con los roles especificados. Requiere permiso USER_CREATE (solo ADMINISTRADOR)."
    )
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ─────────────────────────────────────────────────────
    // PUT /users/{id}
    // ADMINISTRADOR → ✅ puede editar cualquier usuario
    // SUPERVISOR    → ❌ acceso denegado
    // CONTRATISTA   → ❌ acceso denegado
    // AUDITOR       → ❌ acceso denegado
    // ─────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('USER_UPDATE')")
    @Operation(
        summary = "Actualizar usuario",
        description = "Actualiza los datos de un usuario existente. Requiere permiso USER_UPDATE (solo ADMINISTRADOR)."
    )
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    // ─────────────────────────────────────────────────────
    // DELETE /users/{id}
    // ADMINISTRADOR → ✅ puede desactivar usuarios (Soft Delete)
    // SUPERVISOR    → ❌ acceso denegado
    // CONTRATISTA   → ❌ acceso denegado
    // AUDITOR       → ❌ acceso denegado
    //
    // NOTA IMPORTANTE: No eliminamos de la BD.
    // Se hace un Soft Delete (active = false) para preservar
    // la integridad de los registros de auditoría históricos.
    // ─────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('USER_DELETE')")
    @Operation(
        summary = "Desactivar usuario",
        description = "Desactiva un usuario del sistema (soft delete). No se elimina de la BD. Requiere permiso USER_DELETE (solo ADMINISTRADOR)."
    )
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────
    // PATCH /users/{id}/reactivar
    // ADMINISTRADOR → ✅ puede reactivar usuarios desactivados
    // SUPERVISOR    → ❌ acceso denegado
    // CONTRATISTA   → ❌ acceso denegado
    // AUDITOR       → ❌ acceso denegado
    // ─────────────────────────────────────────────────────
    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('USER_UPDATE')")
    @Operation(
        summary = "Reactivar usuario",
        description = "Reactiva un usuario que había sido desactivado. Requiere permiso USER_UPDATE (solo ADMINISTRADOR)."
    )
    public ResponseEntity<UserResponse> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(userService.reactivate(id));
    }
}
