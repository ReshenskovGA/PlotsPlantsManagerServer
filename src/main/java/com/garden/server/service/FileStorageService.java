package com.garden.server.service;

import com.garden.server.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileStorageConfig fileStorageConfig;
    private Logger log;

    public List<String> storeFiles(List<MultipartFile> files) {
        List<String> savedFileNames = new ArrayList<>();
        if (files == null || files.isEmpty()) return savedFileNames;

        try {
            Path uploadDir = Paths.get(fileStorageConfig.getDir()).toAbsolutePath().normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String extension = originalFilename != null && originalFilename.contains(".")
                            ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                    String fileName = UUID.randomUUID().toString() + extension;

                    Path targetLocation = uploadDir.resolve(fileName);
                    file.transferTo(targetLocation);
                    savedFileNames.add(fileName);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Не удалось сохранить файлы: " + ex.getMessage());
        }
        return savedFileNames;
    }
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        try {
            Path uploadDir = Paths.get(fileStorageConfig.getDir()).toAbsolutePath().normalize();
            Path filePath = uploadDir.resolve(fileName).normalize();
            if (filePath.startsWith(uploadDir)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ex) {
            log.warn("Не удалось удалить файл {}: {}", fileName, ex.getMessage());
        }
    }
}