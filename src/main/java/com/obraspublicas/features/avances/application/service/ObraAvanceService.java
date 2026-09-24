package com.obraspublicas.features.avances.application.service;

import com.obraspublicas.features.avances.domain.model.AvanceEvidencia;
import com.obraspublicas.features.avances.domain.model.FaseEvidencia;
import com.obraspublicas.features.avances.domain.model.ObraAvance;
import com.obraspublicas.features.avances.domain.model.TipoEvidencia;
import com.obraspublicas.features.avances.domain.repository.ObraAvanceRepository;
import com.obraspublicas.features.audit.application.service.AuditService;
import com.obraspublicas.features.obras.domain.model.ObraMeta;
import com.obraspublicas.features.obras.domain.model.ObraMetaEstado;
import com.obraspublicas.features.obras.domain.repository.ObraMetaRepository;
import com.obraspublicas.features.obras.domain.model.Obra;
import com.obraspublicas.features.obras.domain.repository.ObraRepository;
import com.obraspublicas.shared.exception.ResourceNotFoundException;
import com.obraspublicas.shared.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ObraAvanceService {

    private final ObraAvanceRepository repository;
    private final ObraMetaRepository metaRepository;
    private final ObraRepository obraRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;

    private static final Map<FaseEvidencia, String> NOMBRE_FASE = Map.of(
        FaseEvidencia.ANTES,   "Antes de la obra",
        FaseEvidencia.DURANTE, "Durante la obra",
        FaseEvidencia.DESPUES, "Después de la obra"
    );

    @Transactional
    public ObraAvance registrarAvance(ObraAvance avance) {
        Obra obra = obraRepository.findById(avance.getObraId())
                .orElseThrow(() -> new ResourceNotFoundException("Obra", "id", avance.getObraId()));

        if (avance.getFechaAvance() != null) {
            if (avance.getFechaAvance().isBefore(obra.getFechaInicio()) || avance.getFechaAvance().isAfter(obra.getFechaFin())) {
                throw new IllegalArgumentException("La fecha del avance debe estar entre " + obra.getFechaInicio() + " y " + obra.getFechaFin());
            }
        }

        String descripcionMeta = "";
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

            descripcionMeta = " en la meta \"" + meta.getConcepto() + "\" (" + avance.getCantidadEjecutada() + " " + meta.getUnidadMedida() + ", acumulado: " + nuevoAcumulado + ")";
        }

        if (avance.getPorcentaje() != null) {
            int pct = Math.max(0, Math.min(100, avance.getPorcentaje()));
            Integer ultimoPct = consultarUltimoPorcentaje(avance.getObraId());
            if (pct < ultimoPct) {
                throw new IllegalArgumentException("El porcentaje de avance (" + pct + "%) no puede ser menor al avance actual (" + ultimoPct + "%).");
            }
            avance.setPorcentaje(pct);
        }

        ObraAvance saved = repository.save(avance);

        auditService.registrarEvento(
            avance.getObraId(),
            "REGISTRO_AVANCE",
            "Se registró un avance de obra" + descripcionMeta + " — Fecha: " + avance.getFechaAvance(),
            "OK",
            null,
            saved
        );

        return saved;
    }

    public List<ObraAvance> listarCronologico(Long obraId) {
        return repository.findByObraIdChronological(obraId);
    }
    
    public Integer consultarUltimoPorcentaje(Long obraId) {
        return repository.findUltimoAvance(obraId).map(ObraAvance::getPorcentaje).orElse(0);
    }

    @Transactional
    public AvanceEvidencia subirEvidencia(Long avanceId, MultipartFile file, FaseEvidencia fase, String descripcion) {
        // Obtener el obraId desde el avance para el registro de auditoría
        ObraAvance avance = repository.findById(avanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Avance", "id", avanceId));

        String storedName = fileStorageService.storeFile(file, "evidencia_" + avanceId);
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        TipoEvidencia tipo = contentType.startsWith("video/") ? TipoEvidencia.VIDEO : TipoEvidencia.FOTO;
        String tipoTexto = tipo == TipoEvidencia.VIDEO ? "video" : "imagen/foto";

        AvanceEvidencia evidencia = AvanceEvidencia.builder()
                .avanceId(avanceId)
                .archivoUrl(storedName)
                .tipo(tipo)
                .fase(fase)
                .descripcion(descripcion)
                .build();

        AvanceEvidencia saved = repository.saveEvidencia(evidencia);

        String faseNombre = NOMBRE_FASE.getOrDefault(fase, fase.name());
        auditService.registrarEvento(
            avance.getObraId(),
            "SUBIDA_EVIDENCIA",
            "Se subió una " + tipoTexto + " a la Línea de Tiempo — Fase: \"" + faseNombre + "\", Descripción: \"" + (descripcion != null ? descripcion : file.getOriginalFilename()) + "\"",
            "OK",
            null,
            saved
        );

        return saved;
    }

    @Transactional
    public void eliminarAvance(Long avanceId) {
        ObraAvance avance = repository.findById(avanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Avance", "id", avanceId));

        repository.deleteById(avanceId);

        auditService.registrarEvento(
            avance.getObraId(),
            "ELIMINACION_AVANCE",
            "Se eliminó el avance del " + avance.getFechaAvance() + " — Título: \"" + avance.getTitulo() + "\"",
            "OK",
            avance,
            null
        );
    }

    @Transactional
    public void eliminarEvidencia(Long evidenciaId) {
        repository.deleteEvidenciaById(evidenciaId);
    }
}
