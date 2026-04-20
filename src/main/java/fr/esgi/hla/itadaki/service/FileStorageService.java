package fr.esgi.hla.itadaki.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for file storage operations for meal images.
 * - store(MultipartFile file)         → String (stored file path or URL)
 * - delete(String filePath)           → void
 * - getFileUrl(String filePath)       → String (public URL)
 * Implementation may use local filesystem or cloud storage (S3, etc.).
 */
public interface FileStorageService {

    String store(MultipartFile file);

    void delete(String filePath);

    String getFileUrl(String filePath);
}
