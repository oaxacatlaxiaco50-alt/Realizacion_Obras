package com.obraspublicas.features.rutas.presentation.controller;

import com.obraspublicas.features.rutas.application.mapper.RutaObraMapper;
import com.obraspublicas.features.rutas.application.service.RutaObraService;
import com.obraspublicas.features.rutas.domain.model.RutaObra;
import com.obraspublicas.features.rutas.presentation.request.RutaObraCreateRequest;
import com.obraspublicas.features.rutas.presentation.request.RutaObraUpdateRequest;
import com.obraspublicas.features.rutas.presentation.response.RutaObraResponse;
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
@RequestMapping("/rutas")
@RequiredArgsConstructor
@Tag(name = "Rutas", description = "Endpoints para la gestión de rutas trazadas (caminos reales) para las obras públicas")
public class RutaObraController {

    private final RutaObraService rutaObraService;
    private final RutaObraMapper mapper;

    @PostMapping
    @PreAuthorize("hasAuthority('RUTA_CREATE')")
    @Operation(summary = "Crear ruta", description = "Crea una nueva ruta de obra con su listado ordenado de coordenadas. Requiere RUTA_CREATE.")
    public ResponseEntity<RutaObraResponse> crearRuta(@Valid @RequestBody RutaObraCreateRequest request) {
        RutaObra ruta = rutaObraService.crearRuta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(ruta));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RUTA_VIEW')")
    @Operation(summary = "Consultar ruta", description = "Obtiene una ruta por su ID y su listado de coordenadas ordenadas. Requiere RUTA_VIEW.")
    public ResponseEntity<RutaObraResponse> consultarRuta(@PathVariable Long id) {
        RutaObra ruta = rutaObraService.consultarRuta(id);
        return ResponseEntity.ok(mapper.toResponse(ruta));
    }

    @GetMapping("/obra/{obraId}")
    @PreAuthorize("hasAuthority('RUTA_VIEW')")
    @Operation(summary = "Listar rutas por obra", description = "Obtiene todas las rutas asociadas a una obra con sus coordenadas ordenadas para pintar el trazado real en el mapa. Requiere RUTA_VIEW.")
    public ResponseEntity<List<RutaObraResponse>> consultarRutasPorObra(@PathVariable Long obraId) {
        List<RutaObraResponse> rutas = rutaObraService.consultarRutasPorObra(obraId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rutas);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RUTA_UPDATE')")
    @Operation(summary = "Actualizar ruta", description = "Actualiza los datos y coordenadas (puntos) de una ruta de obra existente. Requiere RUTA_UPDATE.")
    public ResponseEntity<RutaObraResponse> actualizarRuta(
            @PathVariable Long id,
            @Valid @RequestBody RutaObraUpdateRequest request
    ) {
        RutaObra ruta = rutaObraService.actualizarRuta(id, request);
        return ResponseEntity.ok(mapper.toResponse(ruta));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('RUTA_DELETE')")
    @Operation(summary = "Eliminar ruta", description = "Elimina una ruta y todos sus puntos en cascada. Requiere RUTA_DELETE.")
    public ResponseEntity<Void> eliminarRuta(@PathVariable Long id) {
        rutaObraService.eliminarRuta(id);
        return ResponseEntity.noContent().build();
    }
}
