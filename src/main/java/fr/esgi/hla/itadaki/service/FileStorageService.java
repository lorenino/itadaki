package fr.esgi.hla.itadaki.service;

import org.springframework.web.multipart.MultipartFile;

/** Stores, deletes, and resolves public URLs for meal image files. */
public interface FileStorageService {

    String store(MultipartFile file);

    void delete(String filePath);

    String getFileUrl(String filePath);
}
