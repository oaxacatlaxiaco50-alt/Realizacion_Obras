package com.obraspublicas.features.expedientes.presentation.controller;

import com.obraspublicas.features.expedientes.application.service.ExpedienteObraService;
import com.obraspublicas.features.expedientes.domain.model.ExpedienteObra;
import com.obraspublicas.features.expedientes.presentation.dto.ExpedienteUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/obras/{obraId}/expediente")
@RequiredArgsConstructor
@Tag(name = "Expediente Técnico", description = "Gestión de los documentos del expediente técnico de una obra")
@SecurityRequirement(name = "bearerAuth")
public class ExpedienteObraController {

    private final ExpedienteObraService service;

    @GetMapping
    @Operation(summary = "Obtener el expediente completo de una obra")
    public ResponseEntity<List<ExpedienteObra>> obtenerExpediente(@PathVariable Long obraId) {
        return ResponseEntity.ok(service.obtenerExpedientePorObra(obraId));
    }

    @PutMapping("/{expedienteId}")
    @Operation(summary = "Actualizar el estado y observaciones de un documento en el expediente")
    public ResponseEntity<ExpedienteObra> actualizarEstado(
            @PathVariable Long obraId,
            @PathVariable Long expedienteId,
            @RequestBody ExpedienteUpdateRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails currentUser) {
        String username = (currentUser != null) ? currentUser.getUsername() : "admin";
        return ResponseEntity.ok(service.actualizarEstado(expedienteId, request.getEstado(), request.getObservaciones(), username));
    }

    @PostMapping(value = "/{expedienteId}/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir un archivo PDF para un documento del expediente")
    public ResponseEntity<ExpedienteObra> subirArchivo(
            @PathVariable Long obraId,
            @PathVariable Long expedienteId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails currentUser) {
        String username = (currentUser != null) ? currentUser.getUsername() : "admin";
        return ResponseEntity.ok(service.subirArchivo(expedienteId, file, username));
    }
}
