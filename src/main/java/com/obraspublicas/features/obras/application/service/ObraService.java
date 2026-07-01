package com.obraspublicas.features.obras.application.service;

import com.obraspublicas.features.audit.application.service.AuditService;
import com.obraspublicas.features.obras.domain.model.Obra;
import com.obraspublicas.features.obras.domain.model.ObraEstatus;
import com.obraspublicas.features.obras.domain.repository.ObraRepository;
import com.obraspublicas.features.obras.presentation.request.ObraCreateRequest;
import com.obraspublicas.features.obras.presentation.request.ObraUpdateRequest;
import com.obraspublicas.features.users.domain.repository.UserRepository;
import com.obraspublicas.shared.exception.BusinessException;
import com.obraspublicas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ObraService {

    private final ObraRepository obraRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public Obra registrarObra(ObraCreateRequest request) {
        if (obraRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe una obra registrada con el código: " + request.getCodigo());
        }

        // Validar que el responsable existe
        userRepository.findById(request.getResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getResponsableId()));

        Obra obra = Obra.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .monto(request.getMonto())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .estatus(request.getEstatus())
                .responsableId(request.getResponsableId())
                .build();

        Obra savedObra = obraRepository.save(obra);

        // Registrar auditoría y bitácora
        auditService.registrarEvento(
                savedObra.getId(),
                "CREACION_OBRA",
                "Se creó la obra con código: " + savedObra.getCodigo(),
                savedObra.getEstatus().name(),
                null,
                savedObra
        );

        return savedObra;
    }

    public Obra consultarObra(Long id) {
        return obraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obra", "id", id));
    }

    @Transactional
    public Obra actualizarObra(Long id, ObraUpdateRequest request) {
        Obra existingObra = consultarObra(id);

        // Clonar datos anteriores para auditoría
        Obra oldObraCopy = cloneObra(existingObra);

        // Validar responsable
        userRepository.findById(request.getResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getResponsableId()));

        // Detectar cambios específicos para determinar acción y descripción de bitácora
        List<String> changeDetails = new ArrayList<>();
        String action = "MODIFICACION_OBRA";

        if (!Objects.equals(existingObra.getResponsableId(), request.getResponsableId())) {
            action = "ASIGNACION_RESPONSABLES";
            changeDetails.add("Responsable cambiado de " + existingObra.getResponsableId() + " a " + request.getResponsableId());
        }

        boolean datesOrMontoChanged = !Objects.equals(existingObra.getMonto(), request.getMonto())
                || !Objects.equals(existingObra.getFechaInicio(), request.getFechaInicio())
                || !Objects.equals(existingObra.getFechaFin(), request.getFechaFin());

        if (datesOrMontoChanged) {
            action = "ACTUALIZACION_MONTOS_FECHAS";
            if (!Objects.equals(existingObra.getMonto(), request.getMonto())) {
                changeDetails.add("Monto cambiado de $" + existingObra.getMonto() + " a $" + request.getMonto());
            }
            if (!Objects.equals(existingObra.getFechaInicio(), request.getFechaInicio())) {
                changeDetails.add("Fecha inicio cambiada de " + existingObra.getFechaInicio() + " a " + request.getFechaInicio());
            }
            if (!Objects.equals(existingObra.getFechaFin(), request.getFechaFin())) {
                changeDetails.add("Fecha fin cambiada de " + existingObra.getFechaFin() + " a " + request.getFechaFin());
            }
        }

        if (changeDetails.isEmpty()) {
            changeDetails.add("Actualización general de información");
        }

        // Actualizar datos
        existingObra.setNombre(request.getNombre());
        existingObra.setDescripcion(request.getDescripcion());
        existingObra.setMonto(request.getMonto());
        existingObra.setFechaInicio(request.getFechaInicio());
        existingObra.setFechaFin(request.getFechaFin());
        existingObra.setResponsableId(request.getResponsableId());

        Obra updatedObra = obraRepository.save(existingObra);

        // Registrar auditoría y bitácora
        auditService.registrarEvento(
                updatedObra.getId(),
                action,
                "Se actualizó la obra: " + String.join(", ", changeDetails),
                updatedObra.getEstatus().name(),
                oldObraCopy,
                updatedObra
        );

        return updatedObra;
    }

    @Transactional
    public Obra cambiarEstatus(Long id, ObraEstatus nuevoEstatus) {
        Obra existingObra = consultarObra(id);

        if (existingObra.getEstatus() == nuevoEstatus) {
            return existingObra;
        }

        Obra oldObraCopy = cloneObra(existingObra);
        ObraEstatus oldEstatus = existingObra.getEstatus();

        existingObra.setEstatus(nuevoEstatus);
        Obra updatedObra = obraRepository.save(existingObra);

        // Registrar auditoría y bitácora
        auditService.registrarEvento(
                updatedObra.getId(),
                "CAMBIO_ESTATUS",
                "Cambio de estatus de " + oldEstatus + " a " + nuevoEstatus,
                nuevoEstatus.name(),
                oldObraCopy,
                updatedObra
        );

        return updatedObra;
    }

    public Page<Obra> consultarObras(
            String codigo,
            String nombre,
            ObraEstatus estatus,
            Long responsableId,
            LocalDate startFechaInicio,
            LocalDate endFechaInicio,
            Pageable pageable
    ) {
        return obraRepository.findAll(codigo, nombre, estatus, responsableId, startFechaInicio, endFechaInicio, pageable);
    }

    private Obra cloneObra(Obra original) {
        return Obra.builder()
                .id(original.getId())
                .codigo(original.getCodigo())
                .nombre(original.getNombre())
                .descripcion(original.getDescripcion())
                .monto(original.getMonto())
                .fechaInicio(original.getFechaInicio())
                .fechaFin(original.getFechaFin())
                .estatus(original.getEstatus())
                .responsableId(original.getResponsableId())
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .build();
    }
}
