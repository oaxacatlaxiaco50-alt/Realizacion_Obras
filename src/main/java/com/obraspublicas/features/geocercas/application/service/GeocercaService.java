package com.obraspublicas.features.geocercas.application.service;

import com.obraspublicas.features.geocercas.domain.model.Geocerca;
import com.obraspublicas.features.geocercas.domain.model.GeocercaPunto;
import com.obraspublicas.features.geocercas.domain.repository.GeocercaRepository;
import com.obraspublicas.features.geocercas.presentation.request.GeocercaCreateRequest;
import com.obraspublicas.features.geocercas.presentation.request.GeocercaUpdateRequest;
import com.obraspublicas.features.audit.application.service.AuditService;
import com.obraspublicas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeocercaService {

    private final GeocercaRepository geocercaRepository;
    private final AuditService auditService;

    @Transactional
    public Geocerca crearGeocerca(GeocercaCreateRequest request) {
        AtomicInteger orden = new AtomicInteger(0);
        List<GeocercaPunto> puntos = request.getPuntos().stream()
                .map(p -> GeocercaPunto.builder()
                        .latitud(p.getLatitud())
                        .longitud(p.getLongitud())
                        .orden(orden.getAndIncrement())
                        .build())
                .collect(Collectors.toList());

        Geocerca geocerca = Geocerca.builder()
                .obraId(request.getObraId())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .puntos(puntos)
                .build();

        Geocerca saved = geocercaRepository.save(geocerca);

        auditService.registrarEvento(
            request.getObraId(),
            "CREACION_GEOCERCA",
            "Se definió una nueva geocerca \"" + request.getNombre() + "\" con " + request.getPuntos().size() + " puntos",
            "OK", null, saved
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public Geocerca consultarGeocerca(Long id) {
        return geocercaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Geocerca", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Geocerca> consultarGeocercasPorObra(Long obraId) {
        return geocercaRepository.findByObraId(obraId);
    }

    @Transactional
    public Geocerca actualizarGeocerca(Long id, GeocercaUpdateRequest request) {
        Geocerca existente = geocercaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Geocerca", "id", id));

        AtomicInteger orden = new AtomicInteger(0);
        List<GeocercaPunto> puntos = request.getPuntos().stream()
                .map(p -> GeocercaPunto.builder()
                        .latitud(p.getLatitud())
                        .longitud(p.getLongitud())
                        .orden(orden.getAndIncrement())
                        .build())
                .collect(Collectors.toList());

        existente.setNombre(request.getNombre());
        existente.setDescripcion(request.getDescripcion());
        existente.setPuntos(puntos);

        Geocerca saved = geocercaRepository.save(existente);

        auditService.registrarEvento(
            existente.getObraId(),
            "MODIFICACION_GEOCERCA",
            "Se modificó la geocerca \"" + request.getNombre() + "\"",
            "OK", null, saved
        );
        return saved;
    }

    @Transactional
    public void eliminarGeocerca(Long id) {
        Geocerca geocerca = geocercaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Geocerca", "id", id));
        geocercaRepository.deleteById(id);
        auditService.registrarEvento(
            geocerca.getObraId(),
            "ELIMINACION_GEOCERCA",
            "Se eliminó la geocerca \"" + geocerca.getNombre() + "\"",
            "OK", geocerca, null
        );
    }
}
