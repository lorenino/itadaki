package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Implementation of FileStorageService using local filesystem storage.
 * Stores files in configured upload directory and provides file URLs.
 */
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public String store(MultipartFile file) {
        try {
            // Generate unique filename
            String filename = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
            Path uploadPath = Paths.get(uploadDir);

            // Create upload directory if it doesn't exist
            Files.createDirectories(uploadPath);

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Return relative path
            return uploadDir + "/" + filename;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete file: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        // Construit une URL relative (servie par ResourceHandler /uploads/**).
        // filePath peut etre "./uploads/uuid.jpg" ou "uploads/uuid.jpg" -- on
        // extrait juste le nom de fichier pour eviter le "./".
        if (filePath == null) return null;
        int slash = filePath.lastIndexOf('/');
        String name = slash >= 0 ? filePath.substring(slash + 1) : filePath;
        return "/uploads/" + name;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
