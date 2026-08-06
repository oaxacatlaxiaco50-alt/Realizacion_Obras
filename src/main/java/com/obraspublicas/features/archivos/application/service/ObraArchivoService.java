package com.obraspublicas.features.archivos.application.service;

import com.obraspublicas.features.archivos.domain.model.CarpetaTipo;
import com.obraspublicas.features.archivos.domain.model.ObraArchivo;
import com.obraspublicas.features.archivos.domain.model.TipoArchivo;
import com.obraspublicas.features.archivos.domain.repository.ObraArchivoRepository;
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

    @Transactional
    public ObraArchivo subirArchivo(Long obraId, CarpetaTipo carpeta, MultipartFile file, Long usuarioId) {
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

        return repository.save(archivo);
    }

    public List<ObraArchivo> listarPorCarpeta(Long obraId, CarpetaTipo carpeta) {
        if (carpeta != null) {
            return repository.findByObraIdAndCarpeta(obraId, carpeta);
        }
        return repository.findByObraId(obraId);
    }

    public Map<String, Long> conteosPorCarpeta(Long obraId) {
        List<ObraArchivo> todos = repository.findByObraId(obraId);
        return todos.stream().collect(
                Collectors.groupingBy(a -> a.getCarpeta().name(), Collectors.counting())
        );
    }

    @Transactional
    public void eliminarArchivo(Long archivoId) {
        if (!repository.existsById(archivoId)) {
            throw new ResourceNotFoundException("Archivo", "id", archivoId);
        }
        repository.deleteById(archivoId);
    }
}
