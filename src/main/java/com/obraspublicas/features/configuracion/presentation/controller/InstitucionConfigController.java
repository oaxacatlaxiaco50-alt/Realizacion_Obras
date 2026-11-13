package com.obraspublicas.features.configuracion.presentation.controller;

import com.obraspublicas.features.configuracion.application.dto.InstitucionConfigRequest;
import com.obraspublicas.features.configuracion.application.dto.InstitucionConfigResponse;
import com.obraspublicas.features.configuracion.application.service.InstitucionConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/configuracion/identidad")
@RequiredArgsConstructor
@Tag(name = "Identidad Institucional", description = "Endpoints para consultar y administrar el logo, membrete y datos del municipio")
public class InstitucionConfigController {

    private final InstitucionConfigService service;

    @GetMapping
    @Operation(summary = "Obtener configuración de identidad institucional (Público)")
    public ResponseEntity<InstitucionConfigResponse> getConfig() {
        return ResponseEntity.ok(service.getConfig());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar datos de identidad institucional (Sólo Admin)")
    public ResponseEntity<InstitucionConfigResponse> updateConfig(@Valid @RequestBody InstitucionConfigRequest request) {
        return ResponseEntity.ok(service.updateConfig(request));
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Subir y actualizar logotipo institucional (Sólo Admin)")
    public ResponseEntity<InstitucionConfigResponse> updateLogo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.updateLogo(file));
    }

    @PostMapping(value = "/membrete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Subir y actualizar hoja membretada institucional para reportes (Sólo Admin)")
    public ResponseEntity<InstitucionConfigResponse> updateMembrete(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.updateMembrete(file));
    }

    @DeleteMapping("/logo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Restablecer logotipo institucional a predeterminado (Sólo Admin)")
    public ResponseEntity<InstitucionConfigResponse> resetLogo() {
        return ResponseEntity.ok(service.resetLogo());
    }

    @DeleteMapping("/membrete")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Restablecer membrete oficial a predeterminado (Sólo Admin)")
    public ResponseEntity<InstitucionConfigResponse> resetMembrete() {
        return ResponseEntity.ok(service.resetMembrete());
    }
}
