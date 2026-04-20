package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TODO: Implements FileStorageService using local filesystem storage.
 *       - store: generate unique filename, copy MultipartFile to upload directory,
 *                return relative path
 *       - delete: remove file from filesystem
 *       - getFileUrl: build public URL from base path + relative path
 *
 *       Upload directory and base URL should come from application.properties.
 *       Inject: (none initially — reads config from @Value or properties bean)
 */
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    // TODO: @Value("${app.upload.dir}") private String uploadDir;
    // TODO: @Value("${app.base-url}") private String baseUrl;

    // TODO: Override store(MultipartFile file) → String
    // TODO: Override delete(String filePath) → void
    // TODO: Override getFileUrl(String filePath) → String
}
