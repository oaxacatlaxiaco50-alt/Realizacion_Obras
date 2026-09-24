package com.obraspublicas.features.obras.application.service;

import com.obraspublicas.features.obras.domain.model.ObraMeta;
import com.obraspublicas.features.obras.domain.model.ObraMetaEstado;
import com.obraspublicas.features.obras.domain.repository.ObraMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ObraMetaService {

    private final ObraMetaRepository repository;

    @Transactional
    public ObraMeta crearMeta(ObraMeta meta) {
        if (meta.getCantidadMeta() == null || meta.getCantidadMeta() <= 0) {
            throw new IllegalArgumentException("La cantidad meta debe ser mayor a 0");
        }
        meta.setAvanceAcumulado(0.0);
        meta.setPorcentaje(0);
        meta.setEstado(ObraMetaEstado.PENDIENTE);
        return repository.save(meta);
    }

    public List<ObraMeta> listarPorObra(Long obraId) {
        return repository.findByObraId(obraId);
    }

    @Transactional
    public void eliminarMeta(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public ObraMeta actualizarAvance(Long metaId, Double cantidadAvanzada, String descripcion) {
        ObraMeta meta = repository.findById(metaId)
            .orElseThrow(() -> new RuntimeException("Meta no encontrada"));
        
        meta.setAvanceAcumulado(meta.getAvanceAcumulado() + cantidadAvanzada);
        if (meta.getAvanceAcumulado() > meta.getCantidadMeta()) {
            meta.setAvanceAcumulado(meta.getCantidadMeta());
        }
        
        int nuevoPorcentaje = (int) Math.round((meta.getAvanceAcumulado() / meta.getCantidadMeta()) * 100);
        meta.setPorcentaje(nuevoPorcentaje);
        
        if (nuevoPorcentaje == 100) {
            meta.setEstado(ObraMetaEstado.COMPLETADO);
        } else if (nuevoPorcentaje > 0) {
            meta.setEstado(ObraMetaEstado.EN_PROCESO);
        }
        
        return repository.save(meta);
    }
}
