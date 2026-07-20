package com.obraspublicas.features.expedientes.application.service;

import com.obraspublicas.features.expedientes.domain.model.CatalogoDocumento;
import com.obraspublicas.features.expedientes.domain.model.EstadoDocumento;
import com.obraspublicas.features.expedientes.domain.model.ExpedienteObra;
import com.obraspublicas.features.expedientes.domain.repository.CatalogoDocumentoRepository;
import com.obraspublicas.features.expedientes.domain.repository.ExpedienteObraRepository;
import com.obraspublicas.features.obras.domain.repository.ObraRepository;
import com.obraspublicas.features.users.domain.repository.UserRepository;
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

    @Transactional
    public void generarExpedienteInicial(Long obraId) {
        if (!obraRepository.findById(obraId).isPresent()) {
            throw new ResourceNotFoundException("Obra", "id", obraId);
        }

        if (expedienteRepository.existsByObraId(obraId)) {
            // Ya se generó previamente
            return;
        }

        List<CatalogoDocumento> catalogoActivo = catalogoRepository.findAllActive();

        List<ExpedienteObra> expedientes = catalogoActivo.stream().map(cat -> 
            ExpedienteObra.builder()
                .obraId(obraId)
                .documento(cat)
                .estado(EstadoDocumento.FALTANTE) // Por defecto
                .build()
        ).collect(Collectors.toList());

        expedienteRepository.saveAll(expedientes);
    }

    public List<ExpedienteObra> obtenerExpedientePorObra(Long obraId) {
        return expedienteRepository.findByObraId(obraId);
    }

    @Transactional
    public ExpedienteObra actualizarEstado(Long expedienteId, EstadoDocumento nuevoEstado, String observaciones, String revisadoPorUsername) {
        ExpedienteObra expediente = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente", "id", expedienteId));

        com.obraspublicas.features.users.domain.model.User user = userRepository.findByUsername(revisadoPorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", revisadoPorUsername));

        expediente.setEstado(nuevoEstado);
        expediente.setObservaciones(observaciones);
        expediente.setFechaRevision(LocalDateTime.now());
        expediente.setRevisadoPorId(user.getId());

        return expedienteRepository.save(expediente);
    }

    @Transactional
    public ExpedienteObra subirArchivo(Long expedienteId, MultipartFile archivo, String revisadoPorUsername) {
        ExpedienteObra expediente = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente", "id", expedienteId));

        com.obraspublicas.features.users.domain.model.User user = userRepository.findByUsername(revisadoPorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", revisadoPorUsername));

        String nombreArchivo = fileStorageService.storeFile(archivo, "obra_" + expediente.getObraId() + "_doc_" + expediente.getDocumento().getId());
        
        expediente.setArchivoUrl(nombreArchivo);
        expediente.setEstado(EstadoDocumento.OK); // Autocompletar a OK si sube el archivo
        expediente.setFechaRevision(LocalDateTime.now());
        expediente.setRevisadoPorId(user.getId());

        return expedienteRepository.save(expediente);
    }
}
