package com.jose.shareit.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    // 1. La ruta del directorio de subida está definida aquí directamente.
    //    Esto crea una carpeta "uploads" en la raíz de tu proyecto al ejecutarlo.
    private final Path uploadLocation = Paths.get("uploads");

    /**
     * El constructor se asegura de que el directorio de subida exista.
     * Si no puede crearlo, lanzará una excepción y la aplicación no se iniciará.
     */
    public FileStorageServiceImpl() {
        try {
            Files.createDirectories(uploadLocation);
        } catch (IOException e) {
            // Se usa una RuntimeException estándar, sin necesidad de un archivo nuevo.
            throw new RuntimeException("Could not initialize storage directory: " + uploadLocation.toAbsolutePath(), e);
        }
    }

    /**
     * Esta es la implementación del único método definido en tu interfaz.
     */
    @Override
    public String storeFile(MultipartFile file) {
        // Limpia el nombre del archivo para seguridad.
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Comprueba si el archivo está vacío.
        if (file.isEmpty() || originalFilename.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        
        // Comprueba si el nombre del archivo es inválido.
        if (originalFilename.contains("..")) {
            throw new RuntimeException("Cannot store file with relative path outside current directory " + originalFilename);
        }

        try {
            // Genera un nombre de archivo único para evitar conflictos.
            String fileExtension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex >= 0) {
                fileExtension = originalFilename.substring(lastDotIndex);
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // Construye la ruta final y copia el archivo.
            Path destinationFile = this.uploadLocation.resolve(uniqueFileName).normalize().toAbsolutePath();
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            // Devuelve el nombre único del archivo guardado.
            return uniqueFileName;

        } catch (IOException e) {
            // En caso de un error de E/S, lanza una RuntimeException.
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        }
    }
}