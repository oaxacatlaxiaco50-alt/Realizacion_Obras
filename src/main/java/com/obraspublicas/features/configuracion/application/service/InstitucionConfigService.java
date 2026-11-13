package com.obraspublicas.features.configuracion.application.service;

import com.obraspublicas.features.audit.application.service.AuditService;
import com.obraspublicas.features.configuracion.application.dto.InstitucionConfigRequest;
import com.obraspublicas.features.configuracion.application.dto.InstitucionConfigResponse;
import com.obraspublicas.features.configuracion.domain.model.InstitucionConfig;
import com.obraspublicas.features.configuracion.domain.repository.InstitucionConfigRepository;
import com.obraspublicas.shared.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitucionConfigService {

    private final InstitucionConfigRepository repository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public InstitucionConfigResponse getConfig() {
        InstitucionConfig config = repository.get().orElseGet(this::getDefaultConfig);
        return toResponse(config);
    }

    @Transactional
    public InstitucionConfigResponse updateConfig(InstitucionConfigRequest request) {
        InstitucionConfig current = repository.get().orElseGet(this::getDefaultConfig);
        InstitucionConfig oldCopy = InstitucionConfig.builder()
                .nombreMunicipio(current.getNombreMunicipio())
                .lemaOficial(current.getLemaOficial())
                .periodoAdministracion(current.getPeriodoAdministracion())
                .dependencia(current.getDependencia())
                .piePaginaReporte(current.getPiePaginaReporte())
                .build();

        current.setNombreMunicipio(request.getNombreMunicipio().trim());
        current.setLemaOficial(request.getLemaOficial() != null ? request.getLemaOficial().trim() : null);
        current.setPeriodoAdministracion(request.getPeriodoAdministracion() != null ? request.getPeriodoAdministracion().trim() : null);
        current.setDependencia(request.getDependencia() != null ? request.getDependencia().trim() : null);
        current.setPiePaginaReporte(request.getPiePaginaReporte() != null ? request.getPiePaginaReporte().trim() : null);
        current.setUpdatedAt(LocalDateTime.now());

        InstitucionConfig saved = repository.save(current);
        auditService.registrarEventoUsuario("CONFIG_ACTUALIZADA", "Actualización de datos de identidad institucional", oldCopy, saved);
        return toResponse(saved);
    }

    @Transactional
    public InstitucionConfigResponse updateLogo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo del logotipo no puede estar vacío");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen válida (PNG, JPG, SVG, WebP)");
        }

        InstitucionConfig current = repository.get().orElseGet(this::getDefaultConfig);
        String storedFileName = fileStorageService.storeFile(file, "logo_institucional");
        String logoUrl = "/uploads/" + storedFileName;

        String oldLogo = current.getLogoUrl();
        current.setLogoUrl(logoUrl);
        current.setUpdatedAt(LocalDateTime.now());

        InstitucionConfig saved = repository.save(current);
        auditService.registrarEventoUsuario("LOGO_ACTUALIZADO", "Actualización del logotipo institucional", oldLogo, logoUrl);
        return toResponse(saved);
    }

    @Transactional
    public InstitucionConfigResponse updateMembrete(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo del membrete no puede estar vacío");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/") && !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("El membrete debe ser una imagen válida (PNG, JPG) o PDF");
        }

        InstitucionConfig current = repository.get().orElseGet(this::getDefaultConfig);
        String storedFileName = fileStorageService.storeFile(file, "membrete_institucional");
        String membreteUrl = "/uploads/" + storedFileName;

        String oldMembrete = current.getMembreteUrl();
        current.setMembreteUrl(membreteUrl);
        current.setUpdatedAt(LocalDateTime.now());

        InstitucionConfig saved = repository.save(current);
        auditService.registrarEventoUsuario("MEMBRETE_ACTUALIZADO", "Actualización de la hoja membretada institucional", oldMembrete, membreteUrl);
        return toResponse(saved);
    }

    @Transactional
    public InstitucionConfigResponse resetLogo() {
        InstitucionConfig current = repository.get().orElseGet(this::getDefaultConfig);
        String oldLogo = current.getLogoUrl();
        current.setLogoUrl("/assets/logos/logo.png");
        current.setUpdatedAt(LocalDateTime.now());
        InstitucionConfig saved = repository.save(current);
        auditService.registrarEventoUsuario("LOGO_RESTABLECIDO", "Restablecimiento de logotipo predeterminado", oldLogo, current.getLogoUrl());
        return toResponse(saved);
    }

    @Transactional
    public InstitucionConfigResponse resetMembrete() {
        InstitucionConfig current = repository.get().orElseGet(this::getDefaultConfig);
        String oldMembrete = current.getMembreteUrl();
        current.setMembreteUrl(null);
        current.setUpdatedAt(LocalDateTime.now());
        InstitucionConfig saved = repository.save(current);
        auditService.registrarEventoUsuario("MEMBRETE_RESTABLECIDO", "Restablecimiento de membrete predeterminado", oldMembrete, null);
        return toResponse(saved);
    }

    private InstitucionConfig getDefaultConfig() {
        return InstitucionConfig.builder()
                .id(1L)
                .nombreMunicipio("H. Ayuntamiento Constitucional de Heroica Ciudad de Tlaxiaco")
                .lemaOficial("El poder de servir al pueblo")
                .periodoAdministracion("Administración 2025 - 2027")
                .dependencia("Dirección de Obras Públicas y Desarrollo Urbano")
                .logoUrl("/assets/logos/logo.png")
                .membreteUrl(null)
                .piePaginaReporte("Palacio Municipal s/n, Centro, Heroica Ciudad de Tlaxiaco, Oaxaca.")
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private InstitucionConfigResponse toResponse(InstitucionConfig c) {
        return InstitucionConfigResponse.builder()
                .id(c.getId())
                .nombreMunicipio(c.getNombreMunicipio())
                .lemaOficial(c.getLemaOficial())
                .periodoAdministracion(c.getPeriodoAdministracion())
                .dependencia(c.getDependencia())
                .logoUrl(c.getLogoUrl())
                .membreteUrl(c.getMembreteUrl())
                .piePaginaReporte(c.getPiePaginaReporte())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
