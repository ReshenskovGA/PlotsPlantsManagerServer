package com.garden.server.controller;

import com.garden.server.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/photos")
    public ResponseEntity<List<String>> uploadPhotos(@RequestParam("files") List<MultipartFile> files) {
        // FileStorageService.storeFiles уже возвращает List<String> с именами сохраненных файлов
        List<String> savedFileNames = fileStorageService.storeFiles(files);
        return ResponseEntity.ok(savedFileNames);
    }
}