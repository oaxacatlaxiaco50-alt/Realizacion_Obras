package com.obraspublicas.features.avances.presentation.controller;

import com.obraspublicas.features.avances.application.service.ObraAvanceService;
import com.obraspublicas.features.avances.domain.model.AvanceEvidencia;
import com.obraspublicas.features.avances.domain.model.FaseEvidencia;
import com.obraspublicas.features.avances.domain.model.ObraAvance;
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

@RestController
@RequestMapping("/obras/{obraId}/avances")
@RequiredArgsConstructor
@Tag(name = "Avances de Obra", description = "Línea de tiempo y reporte de avance con fotos/videos")
@SecurityRequirement(name = "bearerAuth")
public class ObraAvanceController {

    private final ObraAvanceService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','CONTRATISTA') or hasAuthority('OBRA_UPDATE')")
    @Operation(summary = "Registrar nuevo avance")
    public ResponseEntity<ObraAvance> registrarAvance(
            @PathVariable Long obraId,
            @RequestBody ObraAvance avance,
            @AuthenticationPrincipal UserDetails user
    ) {
        avance.setObraId(obraId);
        return ResponseEntity.ok(service.registrarAvance(avance));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','CONTRATISTA','AUDITOR') or hasAuthority('OBRA_VIEW')")
    @Operation(summary = "Listar avances cronológicamente (con sus evidencias)")
    public ResponseEntity<List<ObraAvance>> listar(@PathVariable Long obraId) {
        return ResponseEntity.ok(service.listarCronologico(obraId));
    }

    @GetMapping("/ultimo-porcentaje")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','CONTRATISTA','AUDITOR') or hasAuthority('OBRA_VIEW')")
    @Operation(summary = "Obtener el último % de avance de la obra")
    public ResponseEntity<Integer> ultimoPorcentaje(@PathVariable Long obraId) {
        return ResponseEntity.ok(service.consultarUltimoPorcentaje(obraId));
    }

    @PostMapping(value = "/{avanceId}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','CONTRATISTA') or hasAuthority('OBRA_UPDATE')")
    @Operation(summary = "Subir foto o video como evidencia a un avance")
    public ResponseEntity<AvanceEvidencia> subirEvidencia(
            @PathVariable Long obraId,
            @PathVariable Long avanceId,
            @RequestParam("file") MultipartFile file,
            @RequestParam FaseEvidencia fase,
            @RequestParam(required = false) String descripcion
    ) {
        return ResponseEntity.ok(service.subirEvidencia(avanceId, file, fase, descripcion));
    }

    @DeleteMapping("/{avanceId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','CONTRATISTA') or hasAuthority('OBRA_UPDATE')")
    @Operation(summary = "Eliminar avance completo")
    public ResponseEntity<Void> eliminarAvance(
            @PathVariable Long obraId,
            @PathVariable Long avanceId
    ) {
        service.eliminarAvance(avanceId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/evidencias/{evidenciaId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','CONTRATISTA') or hasAuthority('OBRA_UPDATE')")
    @Operation(summary = "Eliminar una evidencia")
    public ResponseEntity<Void> eliminarEvidencia(
            @PathVariable Long obraId,
            @PathVariable Long evidenciaId
    ) {
        service.eliminarEvidencia(evidenciaId);
        return ResponseEntity.noContent().build();
    }
}
