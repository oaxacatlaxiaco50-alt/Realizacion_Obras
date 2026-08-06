package com.obraspublicas.shared.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() {
        // En una app real, esto viene de application.yml (ej. upload.dir)
        // Por ahora, creamos una carpeta en la raíz del proyecto
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio donde los archivos subidos serán guardados.", ex);
        }
    }

    public String storeFile(MultipartFile file, String prefix) {
        try {
            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".") 
                ? originalName.substring(originalName.lastIndexOf(".")) 
                : ".pdf";
                
            String fileName = prefix + "_" + UUID.randomUUID().toString() + extension;
            
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo guardar el archivo. Por favor, inténtelo de nuevo.", ex);
        }
    }

    public org.springframework.core.io.Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Archivo no encontrado o no legible: " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Archivo no encontrado: " + fileName, ex);
        }
    }
}
