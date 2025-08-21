package com.jose.shareit.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Stores a file on the server's disk.
     * @param file The file uploaded by the user.
     * @return The unique filename under which the file was stored.
     */
    String storeFile(MultipartFile file);
}