package com.obraspublicas.features.geocercas.presentation.controller;

import com.obraspublicas.features.geocercas.application.mapper.GeocercaMapper;
import com.obraspublicas.features.geocercas.application.service.GeocercaService;
import com.obraspublicas.features.geocercas.domain.model.Geocerca;
import com.obraspublicas.features.geocercas.presentation.request.GeocercaCreateRequest;
import com.obraspublicas.features.geocercas.presentation.request.GeocercaUpdateRequest;
import com.obraspublicas.features.geocercas.presentation.response.GeocercaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/geocercas")
@RequiredArgsConstructor
@Tag(name = "Geocercas", description = "Endpoints para gestionar geocercas (zonas geográficas) asociadas a obras")
public class GeocercaController {

    private final GeocercaService geocercaService;
    private final GeocercaMapper mapper;

    @PostMapping
    @PreAuthorize("hasAuthority('GEOCERCA_CREATE')")
    @Operation(summary = "Crear geocerca", description = "Crea una nueva geocerca (polígono de puntos) asociada a una obra. Requiere GEOCERCA_CREATE.")
    public ResponseEntity<GeocercaResponse> crearGeocerca(@Valid @RequestBody GeocercaCreateRequest request) {
        Geocerca geocerca = geocercaService.crearGeocerca(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(geocerca));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GEOCERCA_VIEW')")
    @Operation(summary = "Consultar geocerca", description = "Obtiene una geocerca por su ID junto con todos sus puntos. Requiere GEOCERCA_VIEW.")
    public ResponseEntity<GeocercaResponse> consultarGeocerca(@PathVariable Long id) {
        Geocerca geocerca = geocercaService.consultarGeocerca(id);
        return ResponseEntity.ok(mapper.toResponse(geocerca));
    }

    @GetMapping("/obra/{obraId}")
    @PreAuthorize("hasAuthority('GEOCERCA_VIEW')")
    @Operation(summary = "Listar geocercas por obra", description = "Obtiene todas las geocercas asociadas a una obra. Requiere GEOCERCA_VIEW.")
    public ResponseEntity<List<GeocercaResponse>> consultarGeocercasPorObra(@PathVariable Long obraId) {
        List<GeocercaResponse> geocercas = geocercaService.consultarGeocercasPorObra(obraId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(geocercas);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('GEOCERCA_UPDATE')")
    @Operation(summary = "Actualizar geocerca", description = "Actualiza los datos y los puntos de una geocerca existente. Requiere GEOCERCA_UPDATE.")
    public ResponseEntity<GeocercaResponse> actualizarGeocerca(
            @PathVariable Long id,
            @Valid @RequestBody GeocercaUpdateRequest request
    ) {
        Geocerca geocerca = geocercaService.actualizarGeocerca(id, request);
        return ResponseEntity.ok(mapper.toResponse(geocerca));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GEOCERCA_DELETE')")
    @Operation(summary = "Eliminar geocerca", description = "Elimina una geocerca y todos sus puntos en cascada. Requiere GEOCERCA_DELETE.")
    public ResponseEntity<Void> eliminarGeocerca(@PathVariable Long id) {
        geocercaService.eliminarGeocerca(id);
        return ResponseEntity.noContent().build();
    }
}
