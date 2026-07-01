package com.obraspublicas.features.audit.presentation.controller;

import com.obraspublicas.features.audit.application.mapper.AuditMapper;
import com.obraspublicas.features.audit.domain.repository.BitacoraRepository;
import com.obraspublicas.features.audit.presentation.response.BitacoraResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bitacoras")
@RequiredArgsConstructor
@Tag(name = "Bitácora", description = "Endpoints para la consulta de bitácoras de obras")
public class BitacoraController {

    private final BitacoraRepository bitacoraRepository;
    private final AuditMapper mapper;

    @GetMapping
    @PreAuthorize("hasAuthority('BITACORA_VIEW')")
    @Operation(summary = "Consultar bitácoras de obras", description = "Devuelve el listado paginado de entradas de bitácora. Permite filtrar opcionalmente por obraId.")
    public ResponseEntity<Page<BitacoraResponse>> getBitacoras(
            @RequestParam(required = false) Long obraId,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable
    ) {
        Page<BitacoraResponse> response;
        if (obraId != null) {
            response = bitacoraRepository.findByObraId(obraId, pageable).map(mapper::toResponse);
        } else {
            response = bitacoraRepository.findAll(pageable).map(mapper::toResponse);
        }
        return ResponseEntity.ok(response);
    }
}
