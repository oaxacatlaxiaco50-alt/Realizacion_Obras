package com.obraspublicas.features.archivos.application.service;

import com.obraspublicas.features.archivos.domain.model.CarpetaTipo;
import com.obraspublicas.features.archivos.domain.model.ObraArchivo;
import com.obraspublicas.features.archivos.domain.model.TipoArchivo;
import com.obraspublicas.features.archivos.domain.repository.ObraArchivoRepository;
import com.obraspublicas.features.audit.application.service.AuditService;
import com.obraspublicas.shared.exception.BusinessException;
import com.obraspublicas.shared.exception.ResourceNotFoundException;
import com.obraspublicas.shared.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ObraArchivoService {

    private final ObraArchivoRepository repository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;

    private static final Map<CarpetaTipo, String> NOMBRE_CARPETA = Map.of(
        CarpetaTipo.LEGAL,            "Documentos Legales",
        CarpetaTipo.SOCIAL,           "Parte Social",
        CarpetaTipo.TECNICOS,         "Documentos Técnicos",
        CarpetaTipo.ANEXO_FOTOGRAFICO,"Evidencia Fotográfica"
    );

    @Transactional
    public ObraArchivo subirArchivo(Long obraId, CarpetaTipo carpeta, MultipartFile file, Long usuarioId) {
        String originalFilename = file.getOriginalFilename();
        
        // Validación y análisis de duplicados por nombre y tamaño de archivo
        List<ObraArchivo> existentes = repository.findByObraId(obraId);
        boolean esDuplicado = existentes.stream().anyMatch(a -> 
            (originalFilename != null && originalFilename.equalsIgnoreCase(a.getNombreOriginal())) ||
            (file.getSize() > 0 && a.getTamanioBytes() != null && a.getTamanioBytes() == file.getSize() && originalFilename != null && originalFilename.equalsIgnoreCase(a.getNombreOriginal()))
        );

        if (esDuplicado) {
            String seccion = NOMBRE_CARPETA.getOrDefault(carpeta, carpeta.name());
            auditService.registrarEvento(
                obraId,
                "RECHAZO_DOCUMENTO_DUPLICADO",
                "Se analizó y RECHAZÓ el documento duplicado: \"" + originalFilename + "\" en la sección: " + seccion,
                "RECHAZADO",
                null,
                null
            );
            throw new BusinessException("Documento duplicado detectado: El archivo '" + originalFilename + "' ya fue subido anteriormente en esta obra. El sistema analizó y rechazó el documento.");
        }

        String storedName = fileStorageService.storeFile(file, "obra_" + obraId + "_" + carpeta.name().toLowerCase());
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        TipoArchivo tipo;
        if (contentType.contains("pdf")) tipo = TipoArchivo.PDF;
        else if (contentType.contains("word") || contentType.contains("document")) tipo = TipoArchivo.WORD;
        else if (contentType.contains("excel") || contentType.contains("spreadsheet")) tipo = TipoArchivo.EXCEL;
        else if (contentType.startsWith("image/")) tipo = TipoArchivo.IMAGEN;
        else if (contentType.startsWith("video/")) tipo = TipoArchivo.VIDEO;
        else tipo = TipoArchivo.OTRO;

        ObraArchivo archivo = ObraArchivo.builder()
                .obraId(obraId)
                .carpeta(carpeta)
                .nombreOriginal(file.getOriginalFilename())
                .archivoUrl("/uploads/" + storedName)
                .tipoArchivo(tipo)
                .tamanioBytes(file.getSize())
                .subidoPor(usuarioId)
                .build();

        ObraArchivo saved = repository.save(archivo);

        String seccion = NOMBRE_CARPETA.getOrDefault(carpeta, carpeta.name());
        auditService.registrarEvento(
            obraId,
            "SUBIDA_ARCHIVO",
            "Se subió el archivo \"" + file.getOriginalFilename() + "\" en la sección: " + seccion,
            "OK",
            null,
            saved
        );

        return saved;
    }

    public List<ObraArchivo> listarPorCarpeta(Long obraId, CarpetaTipo carpeta) {
        List<ObraArchivo> list = (carpeta != null)
                ? repository.findByObraIdAndCarpeta(obraId, carpeta)
                : repository.findByObraId(obraId);

        list.forEach(a -> {
            if (a.getArchivoUrl() != null && !a.getArchivoUrl().startsWith("/uploads/")) {
                a.setArchivoUrl("/uploads/" + a.getArchivoUrl());
            }
        });
        return list;
    }

    public Map<String, Long> conteosPorCarpeta(Long obraId) {
        List<ObraArchivo> todos = repository.findByObraId(obraId);
        return todos.stream().collect(
                Collectors.groupingBy(a -> a.getCarpeta().name(), Collectors.counting())
        );
    }

    @Transactional
    public void eliminarArchivo(Long archivoId) {
        ObraArchivo archivo = repository.findById(archivoId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo", "id", archivoId));
        String nombreArchivo = archivo.getNombreOriginal();
        String seccion = NOMBRE_CARPETA.getOrDefault(archivo.getCarpeta(), archivo.getCarpeta().name());
        Long obraId = archivo.getObraId();

        repository.deleteById(archivoId);

        auditService.registrarEvento(
            obraId,
            "ELIMINACION_ARCHIVO",
            "Se eliminó el archivo \"" + nombreArchivo + "\" de la sección: " + seccion,
            "OK",
            archivo,
            null
        );
    }
}
