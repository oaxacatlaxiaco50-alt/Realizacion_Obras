package com.obraspublicas.features.obras.application.service;

import com.obraspublicas.features.audit.application.service.AuditService;
import com.obraspublicas.features.expedientes.application.service.ExpedienteObraService;
import com.obraspublicas.features.notificaciones.application.service.NotificacionService;
import com.obraspublicas.features.obras.domain.model.Obra;
import com.obraspublicas.features.obras.domain.model.ObraEstatus;
import com.obraspublicas.features.obras.domain.repository.ObraRepository;
import com.obraspublicas.features.obras.presentation.request.ObraCreateRequest;
import com.obraspublicas.features.obras.presentation.request.ObraUpdateRequest;
import com.obraspublicas.features.users.domain.model.User;
import com.obraspublicas.features.users.domain.repository.UserRepository;
import com.obraspublicas.shared.exception.BusinessException;
import com.obraspublicas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final NotificacionService notificacionService;
    private final com.obraspublicas.features.avances.infrastructure.repository.ObraAvanceJpaRepository obraAvanceJpaRepository;

    @Transactional
    public Obra registrarObra(ObraCreateRequest request) {
        if (obraRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe una obra registrada con el código: " + request.getCodigo());
        }

        // Validar que el responsable existe si fue asignado
        if (request.getResponsableId() != null) {
            userRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getResponsableId()));
        }

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

        Long usuarioActualId = obtenerUsuarioActualId();
        Long responsableId = savedObra.getResponsableId();

        if (responsableId != null) {
            notificacionService.crearNotificacion(
                    responsableId,
                    "Nueva obra asignada",
                    "Se te ha asignado como responsable de la obra: " + savedObra.getNombre(),
                    "info",
                    savedObra.getId(),
                    savedObra.getNombre()
            );
        }

        if (usuarioActualId != null && !Objects.equals(usuarioActualId, responsableId)) {
            notificacionService.crearNotificacion(
                    usuarioActualId,
                    "Obra registrada",
                    "Se ha registrado la obra: " + savedObra.getNombre(),
                    "exito",
                    savedObra.getId(),
                    savedObra.getNombre()
            );
        }

        return savedObra;
    }

    public Obra consultarObra(Long id) {
        return obraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obra", "id", id));
    }

    @Transactional
    public Obra actualizarObra(Long id, ObraUpdateRequest request) {
        Obra existingObra = consultarObra(id);

        // Obtener el responsable anterior antes de modificarlo
        Long responsableAnteriorId = existingObra.getResponsableId();

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

        if (request.getResponsableId() != null && !Objects.equals(responsableAnteriorId, request.getResponsableId())) {
            action = "ASIGNACION_RESPONSABLES";
            changeDetails.add("Responsable cambiado de " + responsableAnteriorId + " a " + request.getResponsableId());
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
        if (request.getFuenteFinanciamiento() != null) existingObra.setFuenteFinanciamiento(request.getFuenteFinanciamiento());
        if (request.getBeneficiarios() != null) existingObra.setBeneficiarios(request.getBeneficiarios());
        if (request.getImpactoSocial() != null) existingObra.setImpactoSocial(request.getImpactoSocial());
        if (request.getContratista() != null) existingObra.setContratista(request.getContratista());

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

        Long nuevoResponsableId = updatedObra.getResponsableId();
        Long usuarioActualId = obtenerUsuarioActualId();
        boolean cambioResponsable = request.getResponsableId() != null
                && !Objects.equals(responsableAnteriorId, nuevoResponsableId);

        if (cambioResponsable) {
            notificacionService.crearNotificacion(
                    nuevoResponsableId,
                    "Nueva obra asignada",
                    "Se te ha asignado como responsable de la obra: " + updatedObra.getNombre(),
                    "info",
                    updatedObra.getId(),
                    updatedObra.getNombre()
            );
        }

        if (usuarioActualId != null && !(cambioResponsable && Objects.equals(usuarioActualId, nuevoResponsableId))) {
            notificacionService.crearNotificacion(
                    usuarioActualId,
                    "Obra actualizada",
                    "Se actualizó la obra: " + updatedObra.getNombre(),
                    "info",
                    updatedObra.getId(),
                    updatedObra.getNombre()
            );
        }

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
        Page<Obra> page = obraRepository.findAll(codigo, nombre, estatus, categoria, responsableId, startFechaInicio, endFechaInicio, pageable);
        page.forEach(this::enrichObraWithPorcentaje);
        return page;
    }

    public Page<Obra> searchGlobal(String keyword, Pageable pageable) {
        Page<Obra> page = obraRepository.searchGlobal(keyword, pageable);
        page.forEach(this::enrichObraWithPorcentaje);
        return page;
    }

    private Obra enrichObraWithPorcentaje(Obra obra) {
        if (obra == null) return null;
        if (obra.getEstatus() == ObraEstatus.COMPLETADA || obra.getEstatus() == ObraEstatus.FINALIZADA) {
            obra.setPorcentajeAvance(100);
        } else {
            Integer pct = obraAvanceJpaRepository.findFirstByObraIdOrderByPorcentajeDesc(obra.getId())
                    .map(com.obraspublicas.features.avances.infrastructure.entity.ObraAvanceEntity::getPorcentaje)
                    .orElse(0);
            obra.setPorcentajeAvance(pct);
        }
        return obra;
    }

    @Transactional
    public void eliminarObraPermanentemente(Long id) {
        Obra existingObra = consultarObra(id);
        if (existingObra.getEstatus() != ObraEstatus.ELIMINADA) {
            throw new BusinessException("Solo se pueden eliminar definitivamente las obras que se encuentran en la Papelera (ELIMINADA).");
        }
        java.time.LocalDateTime fechaPapelera = existingObra.getUpdatedAt() != null
                ? existingObra.getUpdatedAt()
                : existingObra.getCreatedAt();
        if (fechaPapelera != null) {
            long diasEnPapelera = java.time.temporal.ChronoUnit.DAYS.between(fechaPapelera, java.time.LocalDateTime.now());
            if (diasEnPapelera < 30) {
                long diasRestantes = 30 - diasEnPapelera;
                throw new BusinessException("No se puede eliminar definitivamente todavía. La obra debe cumplir al menos 30 días en la papelera (lleva "
                        + diasEnPapelera + " días, faltan " + diasRestantes + " días).");
            }
        }
        obraRepository.deletePermanentById(existingObra.getId());
    }

    public List<Obra> getObrasConCoordenadas() {
        return obraRepository.findAll().stream()
                .filter(o -> o.getLatitud() != null && o.getLongitud() != null)
                .peek(this::enrichObraWithPorcentaje)
                .toList();
    }

    private Long obtenerUsuarioActualId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return userRepository.findByUsername(authentication.getName())
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
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
