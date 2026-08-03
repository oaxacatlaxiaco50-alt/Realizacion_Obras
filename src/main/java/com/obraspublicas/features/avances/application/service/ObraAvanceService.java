package com.obraspublicas.features.avances.application.service;

import com.obraspublicas.features.avances.domain.model.AvanceEvidencia;
import com.obraspublicas.features.avances.domain.model.FaseEvidencia;
import com.obraspublicas.features.avances.domain.model.ObraAvance;
import com.obraspublicas.features.avances.domain.model.TipoEvidencia;
import com.obraspublicas.features.avances.domain.repository.ObraAvanceRepository;
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
    private final FileStorageService fileStorageService;

    @Transactional
    public ObraAvance registrarAvance(ObraAvance avance) {
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
