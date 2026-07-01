package com.example.demo.controller;

import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.service.FileService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/file")
public class JsonFileController {

    private final FileService fileService;

    JsonFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("json/upload")
    public ResponseEntity<?> uploadJson(@RequestParam("file") MultipartFile file) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(
                    new LoginResponseDTO(null, null, "Not authenticated")
            );
        }

        return ResponseEntity.ok(fileService.uploadJson(file, authentication.getName()));
    }

    @GetMapping("json-to-csv/{id}")
    public ResponseEntity<?> convertToCsv(@PathVariable Long id) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(
                    new LoginResponseDTO(null, null, "Not authenticated")
            );
        }

        return ResponseEntity.ok(fileService.jsonToCsv(id, authentication.getName()));
    }

    @GetMapping("download/{id}")
    public ResponseEntity<Resource> downloadCsv(@PathVariable Long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();

        Path filePath = fileService.getDownloadablePath(id, username);
        String downloadFileName = fileService.getCleanDownloadFileName(id, username);

        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
                .body(resource);
    }
}