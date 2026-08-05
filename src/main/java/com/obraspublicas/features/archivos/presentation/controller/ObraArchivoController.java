package com.obraspublicas.features.archivos.presentation.controller;

import com.obraspublicas.features.archivos.application.service.ObraArchivoService;
import com.obraspublicas.features.archivos.domain.model.CarpetaTipo;
import com.obraspublicas.features.archivos.domain.model.ObraArchivo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/obras/{obraId}/archivos")
@RequiredArgsConstructor
@Tag(name = "Carpetas de Obra", description = "Gestión de las 4 carpetas de documentos por obra (Legal, Social, Técnicos, Anexo Fotográfico)")
@SecurityRequirement(name = "bearerAuth")
public class ObraArchivoController {

    private final ObraArchivoService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('OBRA_UPDATE')")
    @Operation(summary = "Subir archivo a una carpeta")
    public ResponseEntity<ObraArchivo> subirArchivo(
            @PathVariable Long obraId,
            @RequestParam CarpetaTipo carpeta,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails user
    ) {
        Long userId = null; // username only available; pass null for now
        return ResponseEntity.ok(service.subirArchivo(obraId, carpeta, file, userId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OBRA_VIEW')")
    @Operation(summary = "Listar archivos de una carpeta (o todas)")
    public ResponseEntity<List<ObraArchivo>> listar(
            @PathVariable Long obraId,
            @RequestParam(required = false) CarpetaTipo carpeta
    ) {
        return ResponseEntity.ok(service.listarPorCarpeta(obraId, carpeta));
    }

    @GetMapping("/conteos")
    @PreAuthorize("hasAuthority('OBRA_VIEW')")
    @Operation(summary = "Contador de archivos por cada carpeta")
    public ResponseEntity<Map<String, Long>> conteos(@PathVariable Long obraId) {
        return ResponseEntity.ok(service.conteosPorCarpeta(obraId));
    }

    @DeleteMapping("/{archivoId}")
    @PreAuthorize("hasAuthority('OBRA_UPDATE')")
    @Operation(summary = "Eliminar un archivo")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long obraId,
            @PathVariable Long archivoId
    ) {
        service.eliminarArchivo(archivoId);
        return ResponseEntity.noContent().build();
    }
}
