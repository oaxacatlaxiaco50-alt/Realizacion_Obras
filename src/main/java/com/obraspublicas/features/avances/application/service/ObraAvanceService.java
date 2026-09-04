package com.obraspublicas.features.avances.application.service;

import com.obraspublicas.features.avances.domain.model.AvanceEvidencia;
import com.obraspublicas.features.avances.domain.model.FaseEvidencia;
import com.obraspublicas.features.avances.domain.model.ObraAvance;
import com.obraspublicas.features.avances.domain.model.TipoEvidencia;
import com.obraspublicas.features.avances.domain.repository.ObraAvanceRepository;
import com.obraspublicas.features.obras.domain.model.ObraMeta;
import com.obraspublicas.features.obras.domain.model.ObraMetaEstado;
import com.obraspublicas.features.obras.domain.repository.ObraMetaRepository;
import com.obraspublicas.shared.exception.ResourceNotFoundException;
import com.obraspublicas.shared.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ObraAvanceService {

    private final ObraAvanceRepository repository;
    private final ObraMetaRepository metaRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ObraAvance registrarAvance(ObraAvance avance) {
        if (avance.getMetaId() != null) {
            ObraMeta meta = metaRepository.findById(avance.getMetaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Meta", "id", avance.getMetaId()));
            
            if (avance.getCantidadEjecutada() == null || avance.getCantidadEjecutada() <= 0) {
                throw new IllegalArgumentException("La cantidad ejecutada debe ser mayor a 0.");
            }
            
            Double nuevoAcumulado = (meta.getAvanceAcumulado() != null ? meta.getAvanceAcumulado() : 0.0) + avance.getCantidadEjecutada();
            
            if (nuevoAcumulado > meta.getCantidadMeta()) {
                throw new IllegalArgumentException("El avance no puede superar la meta total (" + meta.getCantidadMeta() + ").");
            }
            
            meta.setAvanceAcumulado(nuevoAcumulado);
            
            int porcentajeMeta = (int) Math.round((nuevoAcumulado / meta.getCantidadMeta()) * 100);
            meta.setPorcentaje(porcentajeMeta);
            
            if (porcentajeMeta >= 100) {
                meta.setEstado(ObraMetaEstado.COMPLETADO);
            } else if (nuevoAcumulado > 0) {
                meta.setEstado(ObraMetaEstado.EN_PROCESO);
            } else {
                meta.setEstado(ObraMetaEstado.PENDIENTE);
            }
            
            metaRepository.save(meta);
            
            avance.setAcumuladoActual(nuevoAcumulado);
        }

        if (avance.getPorcentaje() != null) {
            int pct = Math.max(0, Math.min(100, avance.getPorcentaje()));
            Integer ultimoPct = consultarUltimoPorcentaje(avance.getObraId());
            if (pct < ultimoPct) {
                throw new IllegalArgumentException("El porcentaje de avance (" + pct + "%) no puede ser menor al avance actual (" + ultimoPct + "%).");
            }
            avance.setPorcentaje(pct);
        }
        return repository.save(avance);
    }

    public List<ObraAvance> listarCronologico(Long obraId) {
        return repository.findByObraIdChronological(obraId);
    }
    
    public Integer consultarUltimoPorcentaje(Long obraId) {
        return repository.findUltimoAvance(obraId).map(ObraAvance::getPorcentaje).orElse(0);
    }

    @Transactional
    public AvanceEvidencia subirEvidencia(Long avanceId, MultipartFile file, FaseEvidencia fase, String descripcion) {
        String storedName = fileStorageService.storeFile(file, "evidencia_" + avanceId);
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        TipoEvidencia tipo = contentType.startsWith("video/") ? TipoEvidencia.VIDEO : TipoEvidencia.FOTO;

        AvanceEvidencia evidencia = AvanceEvidencia.builder()
                .avanceId(avanceId)
                .archivoUrl(storedName)
                .tipo(tipo)
                .fase(fase)
                .descripcion(descripcion)
                .build();

        return repository.saveEvidencia(evidencia);
    }

    @Transactional
    public void eliminarAvance(Long avanceId) {
        repository.deleteById(avanceId);
    }

    @Transactional
    public void eliminarEvidencia(Long evidenciaId) {
        repository.deleteEvidenciaById(evidenciaId);
    }
}
