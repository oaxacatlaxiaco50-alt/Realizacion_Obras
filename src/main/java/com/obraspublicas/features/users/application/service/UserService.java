package com.obraspublicas.features.users.application.service;

import com.obraspublicas.features.audit.application.service.AuditService;
import com.obraspublicas.features.obras.infrastructure.repository.ObraJpaRepository;
import com.obraspublicas.features.roles.infrastructure.entity.RoleEntity;
import com.obraspublicas.features.roles.infrastructure.repository.RoleJpaRepository;
import com.obraspublicas.features.users.domain.repository.UserRepository;
import com.obraspublicas.features.users.infrastructure.entity.UserEntity;
import com.obraspublicas.features.users.infrastructure.repository.UserJpaRepository;
import com.obraspublicas.features.users.presentation.request.UserCreateRequest;
import com.obraspublicas.features.users.presentation.request.UserUpdateRequest;
import com.obraspublicas.features.users.presentation.response.UserResponse;
import com.obraspublicas.shared.exception.BusinessException;
import com.obraspublicas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final ObraJpaRepository obraJpaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    // ─────────────────────────────────────────────────────
    // GET ALL USERS
    // Acceso: USER_VIEW → ADMINISTRADOR, SUPERVISOR
    // ─────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        log.info("Listando todos los usuarios del sistema");
        return userJpaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────
    // GET USER BY ID
    // Acceso: USER_VIEW → ADMINISTRADOR, SUPERVISOR
    // ─────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        log.info("Buscando usuario con ID: {}", id);
        UserEntity entity = userJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return toResponse(entity);
    }

    // ─────────────────────────────────────────────────────
    // CREATE USER
    // Acceso: USER_CREATE → ADMINISTRADOR únicamente
    // ─────────────────────────────────────────────────────
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        log.info("Creando nuevo usuario con username: {}", request.getUsername());

        // Validar duplicados
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Ya existe un usuario con el nombre de usuario: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe un usuario con el correo: " + request.getEmail());
        }

        // Resolver roles desde los nombres enviados
        Set<RoleEntity> roles = resolveRoles(request.getRoles());

        UserEntity newUser = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .roles(roles)
                .build();

        UserEntity saved = userJpaRepository.save(newUser);
        UserResponse response = toResponse(saved);

        String rolesStr = String.join(", ", response.getRoles());
        auditService.registrarEventoUsuario(
                "CREACION_USUARIO",
                "Se creó al usuario " + saved.getFirstName() + " " + saved.getLastName()
                        + " (@" + saved.getUsername() + ") con rol " + rolesStr,
                null,
                response
        );

        log.info("Usuario creado exitosamente con ID: {}", saved.getId());
        return response;
    }

    // ─────────────────────────────────────────────────────
    // UPDATE USER
    // Acceso: USER_UPDATE → ADMINISTRADOR únicamente
    // ─────────────────────────────────────────────────────
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        log.info("Actualizando usuario con ID: {}", id);

        UserEntity entity = userJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        UserResponse previousData = toResponse(entity);

        // Validar email duplicado si fue cambiado
        if (request.getEmail() != null && !request.getEmail().equals(entity.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Ya existe un usuario con el correo: " + request.getEmail());
            }
            entity.setEmail(request.getEmail());
        }

        // Actualizar campos si vienen en el request
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            entity.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            entity.setLastName(request.getLastName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Actualizar roles si fueron enviados
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            entity.setRoles(resolveRoles(request.getRoles()));
        }

        UserEntity updated = userJpaRepository.save(entity);
        UserResponse newData = toResponse(updated);

        auditService.registrarEventoUsuario(
                "MODIFICACION_USUARIO",
                "Se actualizó la información del usuario " + updated.getFirstName() + " " + updated.getLastName()
                        + " (@" + updated.getUsername() + ")",
                previousData,
                newData
        );

        log.info("Usuario ID: {} actualizado correctamente", id);
        return newData;
    }

    // ─────────────────────────────────────────────────────
    // DEACTIVATE USER (Soft Delete)
    // Acceso: USER_DELETE → ADMINISTRADOR únicamente
    // ─────────────────────────────────────────────────────
    @Transactional
    public void deactivate(Long id) {
        log.info("Desactivando usuario con ID: {}", id);

        UserEntity entity = userJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (!entity.isActive()) {
            throw new BusinessException("El usuario con ID " + id + " ya se encuentra inactivo");
        }

        UserResponse previousData = toResponse(entity);
        entity.setActive(false);
        UserEntity saved = userJpaRepository.save(entity);
        UserResponse newData = toResponse(saved);

        auditService.registrarEventoUsuario(
                "DESACTIVACION_USUARIO",
                "Se desactivó la cuenta del usuario " + entity.getFirstName() + " " + entity.getLastName()
                        + " (@" + entity.getUsername() + ")",
                previousData,
                newData
        );

        log.info("Usuario ID: {} desactivado correctamente", id);
    }

    // ─────────────────────────────────────────────────────
    // REACTIVATE USER
    // Acceso: USER_UPDATE → ADMINISTRADOR únicamente
    // ─────────────────────────────────────────────────────
    @Transactional
    public UserResponse reactivate(Long id) {
        log.info("Reactivando usuario con ID: {}", id);

        UserEntity entity = userJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (entity.isActive()) {
            throw new BusinessException("El usuario con ID " + id + " ya se encuentra activo");
        }

        UserResponse previousData = toResponse(entity);
        entity.setActive(true);
        UserEntity reactivated = userJpaRepository.save(entity);
        UserResponse newData = toResponse(reactivated);

        auditService.registrarEventoUsuario(
                "REACTIVACION_USUARIO",
                "Se reactivó la cuenta del usuario " + reactivated.getFirstName() + " " + reactivated.getLastName()
                        + " (@" + reactivated.getUsername() + ")",
                previousData,
                newData
        );

        log.info("Usuario ID: {} reactivado correctamente", id);
        return newData;
    }

    // ─────────────────────────────────────────────────────
    // DELETE USER PERMANENTLY (Opción A: solo si no tiene obras asignadas)
    // Acceso: USER_DELETE → ADMINISTRADOR únicamente
    // ─────────────────────────────────────────────────────
    @Transactional
    public void deletePermanently(Long id) {
        log.info("Intentando eliminar permanentemente al usuario con ID: {}", id);

        UserEntity entity = userJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && entity.getUsername().equalsIgnoreCase(auth.getName())) {
            throw new BusinessException("No puedes eliminar tu propia cuenta mientras tienes la sesión activa.");
        }

        long obrasAsignadas = obraJpaRepository.countByResponsableId(id);
        if (obrasAsignadas > 0) {
            throw new BusinessException("No se puede eliminar al usuario @" + entity.getUsername()
                    + " porque es responsable de " + obrasAsignadas
                    + " obra(s). Primero reasigna sus obras a otro responsable o utiliza la opción 'Desactivar'.");
        }

        UserResponse deletedSnapshot = toResponse(entity);
        String fullName = entity.getFirstName() + " " + entity.getLastName();
        String username = entity.getUsername();
        String rolesStr = String.join(", ", deletedSnapshot.getRoles());

        // Limpiar referencias secundarias del usuario antes de eliminar
        jdbcTemplate.update("DELETE FROM notificaciones WHERE usuario_id = ?", id);
        jdbcTemplate.update("UPDATE expedientes_obras SET revisado_por = NULL WHERE revisado_por = ?", id);
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", id);

        jdbcTemplate.execute("ALTER TABLE bitacoras DISABLE TRIGGER trg_prevent_delete_bitacoras");
        try {
            jdbcTemplate.update("DELETE FROM bitacoras WHERE user_id = ?", id);
        } finally {
            jdbcTemplate.execute("ALTER TABLE bitacoras ENABLE TRIGGER trg_prevent_delete_bitacoras");
        }

        userJpaRepository.delete(entity);

        auditService.registrarEventoUsuario(
                "ELIMINACION_USUARIO",
                "Se eliminó permanentemente al usuario " + fullName + " (@" + username + ") — Rol: " + rolesStr,
                deletedSnapshot,
                null
        );

        log.info("Usuario ID: {} (@{}) eliminado permanentemente", id, username);
    }

    // ─────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────

    private Set<RoleEntity> resolveRoles(Set<String> roleNames) {
        Set<RoleEntity> roles = new HashSet<>();
        for (String roleName : roleNames) {
            RoleEntity role = roleJpaRepository.findByName(roleName.toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", roleName));
            roles.add(role);
        }
        return roles;
    }

    private UserResponse toResponse(UserEntity entity) {
        Set<String> roles = entity.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = entity.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .username(entity.getUsername())
                .active(entity.isActive())
                .roles(roles)
                .permissions(permissions)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
