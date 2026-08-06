package com.obraspublicas.features.obras.application.service;

import com.obraspublicas.features.audit.application.service.AuditService;
import com.obraspublicas.features.expedientes.application.service.ExpedienteObraService;
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
    private final ExpedienteObraService expedienteObraService;

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
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .direccion(request.getDireccion())
                .categoria(request.getCategoria() != null ? request.getCategoria() : "Infraestructura General")
                .build();

        Obra savedObra = obraRepository.save(obra);

        // Generar expediente técnico inicial en estatus FALTANTE
        expedienteObraService.generarExpedienteInicial(savedObra.getId());

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

        // Validar responsable si fue enviado
        if (request.getResponsableId() != null) {
            userRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getResponsableId()));
        }

        // Detectar cambios específicos para determinar acción y descripción de bitácora
        List<String> changeDetails = new ArrayList<>();
        String action = "MODIFICACION_OBRA";

        if (request.getResponsableId() != null && !Objects.equals(existingObra.getResponsableId(), request.getResponsableId())) {
            action = "ASIGNACION_RESPONSABLES";
            changeDetails.add("Responsable cambiado de " + existingObra.getResponsableId() + " a " + request.getResponsableId());
        }

        boolean datesOrMontoChanged = (request.getMonto() != null && !Objects.equals(existingObra.getMonto(), request.getMonto()))
                || (request.getFechaInicio() != null && !Objects.equals(existingObra.getFechaInicio(), request.getFechaInicio()))
                || (request.getFechaFin() != null && !Objects.equals(existingObra.getFechaFin(), request.getFechaFin()));

        if (datesOrMontoChanged) {
            action = "ACTUALIZACION_MONTOS_FECHAS";
        }

        if (changeDetails.isEmpty()) {
            changeDetails.add("Actualización general de información");
        }

        // Actualizar datos solo si vienen no nulos
        if (request.getNombre() != null && !request.getNombre().isBlank()) existingObra.setNombre(request.getNombre());
        if (request.getDescripcion() != null) existingObra.setDescripcion(request.getDescripcion());
        if (request.getMonto() != null) existingObra.setMonto(request.getMonto());
        if (request.getFechaInicio() != null) existingObra.setFechaInicio(request.getFechaInicio());
        if (request.getFechaFin() != null) existingObra.setFechaFin(request.getFechaFin());
        if (request.getResponsableId() != null) existingObra.setResponsableId(request.getResponsableId());
        if (request.getLatitud() != null) existingObra.setLatitud(request.getLatitud());
        if (request.getLongitud() != null) existingObra.setLongitud(request.getLongitud());
        if (request.getDireccion() != null) existingObra.setDireccion(request.getDireccion());
        if (request.getCategoria() != null) existingObra.setCategoria(request.getCategoria());

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
            String categoria,
            Long responsableId,
            LocalDate startFechaInicio,
            LocalDate endFechaInicio,
            Pageable pageable
    ) {
        return obraRepository.findAll(codigo, nombre, estatus, categoria, responsableId, startFechaInicio, endFechaInicio, pageable);
    }

    public Page<Obra> searchGlobal(String keyword, Pageable pageable) {
        return obraRepository.searchGlobal(keyword, pageable);
    }

    public List<Obra> getObrasConCoordenadas() {
        return obraRepository.findAll().stream()
                .filter(o -> o.getLatitud() != null && o.getLongitud() != null)
                .toList();
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
                .latitud(original.getLatitud())
                .longitud(original.getLongitud())
                .direccion(original.getDireccion())
                .categoria(original.getCategoria())
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .build();
    }
}
