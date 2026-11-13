package com.obraspublicas.features.auth.application.service;

import com.obraspublicas.features.auth.presentation.request.LoginRequest;
import com.obraspublicas.features.auth.presentation.response.LoginResponse;
import com.obraspublicas.features.users.domain.repository.UserRepository;
import com.obraspublicas.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generar identificador de sesión único para invalidar cualquier sesión previa en otro equipo
        String sessionId = UUID.randomUUID().toString();
        userRepository.updateActiveSessionId(userDetails.getUsername(), sessionId);

        String token = jwtService.generateToken(userDetails, sessionId);

        // Extract roles (those prefixed with ROLE_)
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toSet());

        // Extract atomic permissions (those NOT prefixed with ROLE_)
        Set<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .collect(Collectors.toSet());

        return LoginResponse.builder()
                .token(token)
                .username(userDetails.getUsername())
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}
