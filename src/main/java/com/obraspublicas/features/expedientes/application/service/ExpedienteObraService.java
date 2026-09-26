package com.obraspublicas.features.expedientes.application.service;

import com.obraspublicas.features.expedientes.domain.model.CatalogoDocumento;
import com.obraspublicas.features.expedientes.domain.model.EstadoDocumento;
import com.obraspublicas.features.expedientes.domain.model.ExpedienteObra;
import com.obraspublicas.features.expedientes.domain.repository.CatalogoDocumentoRepository;
import com.obraspublicas.features.expedientes.domain.repository.ExpedienteObraRepository;
import com.obraspublicas.features.obras.domain.repository.ObraRepository;
import com.obraspublicas.features.audit.application.service.AuditService;
import com.obraspublicas.features.users.domain.repository.UserRepository;
import com.obraspublicas.shared.exception.BusinessException;
import com.obraspublicas.shared.exception.ResourceNotFoundException;
import com.obraspublicas.shared.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpedienteObraService {

    private final ExpedienteObraRepository expedienteRepository;
    private final CatalogoDocumentoRepository catalogoRepository;
    private final ObraRepository obraRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public void generarExpedienteInicial(Long obraId) {
        if (!obraRepository.findById(obraId).isPresent()) {
            throw new ResourceNotFoundException("Obra", "id", obraId);
        }

        List<CatalogoDocumento> catalogoActivo = catalogoRepository.findAllActive();
        List<ExpedienteObra> existentes = expedienteRepository.findByObraId(obraId);
        List<Long> idsExistentes = existentes.stream().map(e -> e.getDocumento().getId()).toList();

        List<ExpedienteObra> nuevos = catalogoActivo.stream()
                .filter(cat -> !idsExistentes.contains(cat.getId()))
                .map(cat -> ExpedienteObra.builder()
                        .obraId(obraId)
                        .documento(cat)
                        .estado(EstadoDocumento.FALTANTE)
                        .build()
                ).collect(Collectors.toList());

        if (!nuevos.isEmpty()) {
            expedienteRepository.saveAll(nuevos);
        }
    }

    @Transactional
    public List<ExpedienteObra> obtenerExpedientePorObra(Long obraId) {
        generarExpedienteInicial(obraId);
        return expedienteRepository.findByObraId(obraId);
    }

    @Transactional
    public ExpedienteObra actualizarEstado(Long expedienteId, EstadoDocumento nuevoEstado, String observaciones, String revisadoPorUsername) {
        ExpedienteObra expediente = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente", "id", expedienteId));

        Long userId = 1L;
        if (revisadoPorUsername != null) {
            userId = userRepository.findByUsername(revisadoPorUsername)
                    .map(com.obraspublicas.features.users.domain.model.User::getId)
                    .orElse(1L);
        }

        expediente.setEstado(nuevoEstado);
        expediente.setObservaciones(observaciones);
        expediente.setFechaRevision(LocalDateTime.now());
        expediente.setRevisadoPorId(userId);

        ExpedienteObra saved = expedienteRepository.save(expediente);

        String nombreDoc = expediente.getDocumento() != null ? expediente.getDocumento().getNombre() : "documento";
        auditService.registrarEvento(
            expediente.getObraId(),
            "ACTUALIZACION_CHECKLIST",
            "Se actualizó el estado de \"" + nombreDoc + "\" a: " + nuevoEstado.name(),
            "OK",
            null,
            saved
        );

        return saved;
    }

    @Transactional
    public ExpedienteObra subirArchivo(Long expedienteId, MultipartFile archivo, String revisadoPorUsername) {
        ExpedienteObra expediente = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente", "id", expedienteId));

        String filename = archivo.getOriginalFilename();
        List<ExpedienteObra> otrosExpedientes = expedienteRepository.findByObraId(expediente.getObraId());
        
        // Verificar si el archivo ya fue subido en este o en otro ítem del expediente de esta obra
        boolean esDuplicado = otrosExpedientes.stream().anyMatch(e -> 
            e.getArchivoUrl() != null && filename != null && e.getArchivoUrl().toLowerCase().endsWith(filename.toLowerCase())
        );

        if (esDuplicado) {
            String nombreDoc = expediente.getDocumento() != null ? expediente.getDocumento().getNombre() : "documento";
            auditService.registrarEvento(
                expediente.getObraId(),
                "RECHAZO_DOCUMENTO_DUPLICADO",
                "Se analizó y RECHAZÓ el archivo duplicado \"" + filename + "\" para el ítem de expediente: \"" + nombreDoc + "\"",
                "RECHAZADO",
                null,
                null
            );
            throw new BusinessException("Documento duplicado detectado: El archivo '" + filename + "' ya se encuentra registrado en el expediente de esta obra. La subida fue rechazada.");
        }

        Long userId = 1L;
        if (revisadoPorUsername != null) {
            userId = userRepository.findByUsername(revisadoPorUsername)
                    .map(com.obraspublicas.features.users.domain.model.User::getId)
                    .orElse(1L);
        }

        String nombreArchivo = fileStorageService.storeFile(archivo, "obra_" + expediente.getObraId() + "_doc_" + expediente.getDocumento().getId());
        
        expediente.setArchivoUrl(nombreArchivo);
        expediente.setEstado(EstadoDocumento.OK);
        expediente.setFechaRevision(LocalDateTime.now());
        expediente.setRevisadoPorId(userId);

        ExpedienteObra saved = expedienteRepository.save(expediente);

        String nombreDoc = expediente.getDocumento() != null ? expediente.getDocumento().getNombre() : "documento";
        auditService.registrarEvento(
            expediente.getObraId(),
            "SUBIDA_DOCUMENTO_CHECKLIST",
            "Se subió el documento \"" + archivo.getOriginalFilename() + "\" al apartado: \"" + nombreDoc + "\" del checklist de integración",
            "OK",
            null,
            saved
        );

        return saved;
    }
}
