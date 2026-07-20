package com.obraspublicas.features.audit.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obraspublicas.features.audit.domain.model.AuditLog;
import com.obraspublicas.features.audit.domain.model.Bitacora;
import com.obraspublicas.features.audit.domain.repository.AuditLogRepository;
import com.obraspublicas.features.audit.domain.repository.BitacoraRepository;
import com.obraspublicas.features.users.domain.model.User;
import com.obraspublicas.features.users.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final BitacoraRepository bitacoraRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Registra un evento en la bitácora y en la auditoría general.
     */
    public void registrarEvento(Long obraId, String action, String description, String status, Object oldData, Object newData) {
        String username = getCurrentUsername();
        Long userId = getUserIdByUsername(username);
        String ip = getClientIp();

        String previousDataJson = null;
        String newDataJson = null;

        try {
            if (oldData != null) {
                previousDataJson = objectMapper.writeValueAsString(oldData);
            }
            if (newData != null) {
                newDataJson = objectMapper.writeValueAsString(newData);
            }
        } catch (Exception e) {
            log.error("Error al serializar datos de auditoría", e);
        }

        // 1. Guardar log de auditoría
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .module("OBRAS")
                .timestamp(LocalDateTime.now())
                .ip(ip)
                .previousData(previousDataJson)
                .newData(newDataJson)
                .build();
        auditLogRepository.save(auditLog);

        // 2. Guardar en bitácora de obra
        Bitacora bitacora = Bitacora.builder()
                .obraId(obraId)
                .description(description)
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .status(status)
                .build();
        bitacoraRepository.save(bitacora);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return "SYSTEM";
    }

    private Long getUserIdByUsername(String username) {
        if ("SYSTEM".equalsIgnoreCase(username)) {
            return 1L; // ID por defecto para cambios del sistema
        }
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(1L); // Fallback
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener la IP del cliente: {}", e.getMessage());
        }
        return "127.0.0.1";
    }
}
