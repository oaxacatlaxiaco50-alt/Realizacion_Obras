package com.obraspublicas.features.obras.presentation.controller;

import com.obraspublicas.features.obras.application.mapper.ObraMapper;
import com.obraspublicas.features.obras.application.service.ObraService;
import com.obraspublicas.features.obras.domain.model.Obra;
import com.obraspublicas.features.obras.domain.model.ObraEstatus;
import com.obraspublicas.features.obras.presentation.request.ObraCreateRequest;
import com.obraspublicas.features.obras.presentation.request.ObraEstatusRequest;
import com.obraspublicas.features.obras.presentation.request.ObraUpdateRequest;
import com.obraspublicas.features.obras.presentation.response.ObraResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/obras")
@RequiredArgsConstructor
@Tag(name = "Obras", description = "Endpoints para la gestión y CRUD de obras públicas")
public class ObraController {

    private final ObraService obraService;
    private final ObraMapper mapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR') or hasAuthority('OBRA_CREATE')")
    @Operation(summary = "Registrar obra", description = "Registra una nueva obra en el sistema. Requiere permiso OBRA_CREATE.")
    public ResponseEntity<ObraResponse> registrarObra(@Valid @RequestBody ObraCreateRequest request) {
        Obra obra = obraService.registrarObra(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(obra));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','CONTRATISTA','AUDITOR') or hasAuthority('OBRA_VIEW')")
    @Operation(summary = "Consultar obra", description = "Obtiene los detalles de una obra por su ID. Requiere permiso OBRA_VIEW.")
    public ResponseEntity<ObraResponse> consultarObra(@PathVariable Long id) {
        Obra obra = obraService.consultarObra(id);
        return ResponseEntity.ok(mapper.toResponse(obra));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SUPERVISOR') or hasAuthority('OBRA_UPDATE')")
    @Operation(summary = "Actualizar obra", description = "Modifica los detalles de una obra.")
    public ResponseEntity<ObraResponse> actualizarObra(
            @PathVariable Long id,
            @Valid @RequestBody ObraUpdateRequest request
    ) {
        Obra obra = obraService.actualizarObra(id, request);
        return ResponseEntity.ok(mapper.toResponse(obra));
    }

    @PatchMapping("/{id}/estatus")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SUPERVISOR') or hasAuthority('OBRA_CHANGE_STATUS') or hasAuthority('OBRA_UPDATE')")
    @Operation(summary = "Cambio de estatus de obra", description = "Permite cambiar el estatus de una obra (ej. PLANIFICADA, EN_PROCESO, CANCELADA, INACTIVA).")
    public ResponseEntity<ObraResponse> cambiarEstatus(
            @PathVariable Long id,
            @Valid @RequestBody ObraEstatusRequest request
    ) {
        Obra obra = obraService.cambiarEstatus(id, request.getEstatus());
        return ResponseEntity.ok(mapper.toResponse(obra));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR') or hasAuthority('OBRA_DELETE') or hasAuthority('OBRA_UPDATE')")
    @Operation(summary = "Eliminar obra permanentemente", description = "Elimina definitivamente una obra y sus registros asociados de la base de datos.")
    public ResponseEntity<Void> eliminarObraPermanentemente(@PathVariable Long id) {
        obraService.eliminarObraPermanentemente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','CONTRATISTA','AUDITOR') or hasAuthority('OBRA_VIEW')")
    @Operation(summary = "Consulta paginada y filtrada", description = "Obtiene una lista paginada de obras filtradas por diversos criterios. Requiere permiso OBRA_VIEW.")
    public ResponseEntity<Page<ObraResponse>> consultarObras(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) ObraEstatus estatus,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startFechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endFechaInicio,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Obra> obrasPage = obraService.consultarObras(
                codigo, nombre, estatus, categoria, responsableId, startFechaInicio, endFechaInicio, pageable
        );
        return ResponseEntity.ok(obrasPage.map(mapper::toResponse));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','CONTRATISTA','AUDITOR') or hasAuthority('OBRA_VIEW')")
    @Operation(summary = "Búsqueda global", description = "Busca obras por una palabra clave que coincida en código, nombre o descripción.")
    public ResponseEntity<Page<ObraResponse>> buscarObras(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Obra> obrasPage = obraService.searchGlobal(q, pageable);
        return ResponseEntity.ok(obrasPage.map(mapper::toResponse));
    }

    @GetMapping("/mapa")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','CONTRATISTA','AUDITOR') or hasAuthority('OBRA_VIEW')")
    @Operation(summary = "Mapa GeoJSON", description = "Retorna todas las obras con coordenadas en formato GeoJSON estándar, listo para Google Maps, Leaflet o Mapbox.")
    public ResponseEntity<java.util.Map<String, Object>> getObrasParaMapa() {
        java.util.List<Obra> obras = obraService.getObrasConCoordenadas();

        java.util.List<java.util.Map<String, Object>> features = obras.stream().map(obra -> {
            java.util.Map<String, Object> geometry = new java.util.LinkedHashMap<>();
            geometry.put("type", "Point");
            geometry.put("coordinates", new double[]{obra.getLongitud(), obra.getLatitud()});

            java.util.Map<String, Object> properties = new java.util.LinkedHashMap<>();
            properties.put("id", obra.getId());
            properties.put("codigo", obra.getCodigo());
            properties.put("nombre", obra.getNombre());
            properties.put("estatus", obra.getEstatus().name());
            properties.put("direccion", obra.getDireccion());

            java.util.Map<String, Object> feature = new java.util.LinkedHashMap<>();
            feature.put("type", "Feature");
            feature.put("geometry", geometry);
            feature.put("properties", properties);
            return feature;
        }).toList();

        java.util.Map<String, Object> geoJson = new java.util.LinkedHashMap<>();
        geoJson.put("type", "FeatureCollection");
        geoJson.put("features", features);

        return ResponseEntity.ok(geoJson);
    }

    @PostMapping("/cierre-fiscal")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ejecutar Cierre Fiscal Anual", description = "Archiva las obras del ejercicio especificado para iniciar el nuevo año en blanco. Solo Administrador.")
    public ResponseEntity<java.util.Map<String, Object>> ejecutarCierreFiscal(@RequestBody java.util.Map<String, Integer> payload) {
        Integer ejercicio = payload.getOrDefault("ejercicio", LocalDate.now().getYear());
        int total = obraService.ejecutarCierreFiscal(ejercicio);
        return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "Cierre fiscal del ejercicio " + ejercicio + " ejecutado con éxito.",
                "ejercicio", ejercicio,
                "obrasArchivadas", total
        ));
    }

    @GetMapping("/archivo-fiscal")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Consultar Archivo Histórico Fiscal", description = "Permite al Administrador y Auditor consultar obras de ejercicios fiscales anteriores archivados.")
    public ResponseEntity<java.util.List<ObraResponse>> consultarArchivoFiscal(
            @RequestParam(required = false) Integer ejercicio,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) String estatus,
            @RequestParam(required = false) String query
    ) {
        java.util.List<Obra> obras = obraService.consultarArchivoFiscal(ejercicio, categoria, mes, estatus, query);
        return ResponseEntity.ok(obras.stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/ejercicios-fiscales")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Obtener Ejercicios Fiscales Disponibles", description = "Lista los años fiscales registrados en el archivo histórico.")
    public ResponseEntity<java.util.List<Integer>> getEjerciciosFiscales() {
        return ResponseEntity.ok(obraService.getEjerciciosFiscalesDisponibles());
    }

    @GetMapping("/resumen-cierre-fiscal/{ejercicio}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Resumen previo al cierre fiscal", description = "Muestra la cantidad de obras pendientes de archivar para un ejercicio.")
    public ResponseEntity<java.util.Map<String, Object>> getResumenCierreFiscal(@PathVariable Integer ejercicio) {
        return ResponseEntity.ok(obraService.getResumenCierreFiscal(ejercicio));
    }
}
