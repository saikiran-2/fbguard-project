package com.fbguard.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stores uploaded files (profile photos, app icons) on local disk under a
 * configurable directory instead of the original project's hardcoded
 * "E:\...\web\apps\" Windows path. Swap this out for an S3Service if you
 * later want uploads to survive container restarts on a host with no
 * persistent disk.
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    public String store(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) return null;
        try {
            Path dir = Path.of(uploadDir, subfolder);
            Files.createDirectories(dir);

            String extension = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf('.'));
            }
            String filename = UUID.randomUUID() + extension;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target);

            return "/uploads/" + subfolder + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Could not store file: " + e.getMessage());
        }
    }
}
