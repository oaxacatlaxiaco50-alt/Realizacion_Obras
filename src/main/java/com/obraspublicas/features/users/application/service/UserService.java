package com.obraspublicas.features.users.application.service;

import com.obraspublicas.features.roles.infrastructure.entity.RoleEntity;
import com.obraspublicas.features.roles.infrastructure.repository.RoleJpaRepository;
import com.obraspublicas.features.users.domain.model.User;
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
    private final PasswordEncoder passwordEncoder;

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
        log.info("Usuario creado exitosamente con ID: {}", saved.getId());
        return toResponse(saved);
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
        log.info("Usuario ID: {} actualizado correctamente", id);
        return toResponse(updated);
    }

    // ─────────────────────────────────────────────────────
    // DEACTIVATE USER (Soft Delete)
    // Acceso: USER_DELETE → ADMINISTRADOR únicamente
    // NOTA: No eliminamos de la BD, desactivamos al usuario
    //       para conservar integridad referencial en auditoría
    // ─────────────────────────────────────────────────────
    @Transactional
    public void deactivate(Long id) {
        log.info("Desactivando usuario con ID: {}", id);

        UserEntity entity = userJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (!entity.isActive()) {
            throw new BusinessException("El usuario con ID " + id + " ya se encuentra inactivo");
        }

        entity.setActive(false);
        userJpaRepository.save(entity);
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

        entity.setActive(true);
        UserEntity reactivated = userJpaRepository.save(entity);
        log.info("Usuario ID: {} reactivado correctamente", id);
        return toResponse(reactivated);
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
