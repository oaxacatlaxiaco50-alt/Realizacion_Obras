package com.obraspublicas.features.rutas.application.service;

import com.obraspublicas.features.rutas.domain.model.PuntoRuta;
import com.obraspublicas.features.rutas.domain.model.RutaObra;
import com.obraspublicas.features.rutas.domain.repository.RutaObraRepository;
import com.obraspublicas.features.rutas.presentation.request.RutaObraCreateRequest;
import com.obraspublicas.features.rutas.presentation.request.RutaObraUpdateRequest;
import com.obraspublicas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RutaObraService {

    private final RutaObraRepository rutaObraRepository;

    @Transactional
    public RutaObra crearRuta(RutaObraCreateRequest request) {
        AtomicInteger orden = new AtomicInteger(0);
        List<PuntoRuta> puntos = request.getPuntos().stream()
                .map(p -> PuntoRuta.builder()
                        .latitud(p.getLatitud())
                        .longitud(p.getLongitud())
                        .orden(orden.getAndIncrement())
                        .build())
                .collect(Collectors.toList());

        RutaObra ruta = RutaObra.builder()
                .obraId(request.getObraId())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .puntos(puntos)
                .build();

        return rutaObraRepository.save(ruta);
    }

    @Transactional(readOnly = true)
    public RutaObra consultarRuta(Long id) {
        return rutaObraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RutaObra", "id", id));
    }

    /**
     * Retorna todas las rutas de una obra con sus puntos ordenados.
     * El frontend consumirá este endpoint para trazar el camino real en el mapa.
     */
    @Transactional(readOnly = true)
    public List<RutaObra> consultarRutasPorObra(Long obraId) {
        return rutaObraRepository.findByObraId(obraId);
    }

    @Transactional
    public RutaObra actualizarRuta(Long id, RutaObraUpdateRequest request) {
        RutaObra existente = rutaObraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RutaObra", "id", id));

        AtomicInteger orden = new AtomicInteger(0);
        List<PuntoRuta> puntos = request.getPuntos().stream()
                .map(p -> PuntoRuta.builder()
                        .latitud(p.getLatitud())
                        .longitud(p.getLongitud())
                        .orden(orden.getAndIncrement())
                        .build())
                .collect(Collectors.toList());

        existente.setNombre(request.getNombre());
        existente.setDescripcion(request.getDescripcion());
        existente.setPuntos(puntos);

        return rutaObraRepository.save(existente);
    }

    @Transactional
    public void eliminarRuta(Long id) {
        if (!rutaObraRepository.existsById(id)) {
            throw new ResourceNotFoundException("RutaObra", "id", id);
        }
        rutaObraRepository.deleteById(id);
    }
}
