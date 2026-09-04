package com.obraspublicas.features.obras.presentation.controller;

import com.obraspublicas.features.obras.application.service.ObraMetaService;
import com.obraspublicas.features.obras.domain.model.ObraMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/obras/{obraId}/metas")
@RequiredArgsConstructor
public class ObraMetaController {

    private final ObraMetaService service;

    @PostMapping
    public ResponseEntity<ObraMeta> crearMeta(@PathVariable Long obraId, @RequestBody ObraMeta meta) {
        meta.setObraId(obraId);
        ObraMeta created = service.crearMeta(meta);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ObraMeta>> listarMetas(@PathVariable Long obraId) {
        List<ObraMeta> metas = service.listarPorObra(obraId);
        return ResponseEntity.ok(metas);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMeta(@PathVariable Long obraId, @PathVariable Long id) {
        service.eliminarMeta(id);
        return ResponseEntity.noContent().build();
    }
}
