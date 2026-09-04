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
}
