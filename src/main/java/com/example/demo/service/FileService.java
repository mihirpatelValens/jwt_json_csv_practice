package com.example.demo.service;

import com.example.demo.entity.FileStatus;
import com.example.demo.entity.JsonMetaDataEntity;
import com.example.demo.exception.FileNotFoundForUserException;
import com.example.demo.exception.FileStorageException;
import com.example.demo.repository.JsonMetadataRepository;
import com.example.demo.util.CsvWriterUtil;
import com.example.demo.util.JsonFlattenerUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileService {

    private static final Path INPUT_ROOT = Paths.get("storage", "input");
    private static final Path OUTPUT_ROOT = Paths.get("storage", "output");

    private final JsonMetadataRepository jsonMetadataRepository;
    private final ObjectMapper objectMapper;

    public FileService(JsonMetadataRepository jsonMetadataRepository, ObjectMapper objectMapper) {
        this.jsonMetadataRepository = jsonMetadataRepository;
        this.objectMapper = objectMapper;
    }

    public JsonMetaDataEntity uploadJson(MultipartFile file, String uploadedBy) throws IOException {

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new FileStorageException("Uploaded file has no name");
        }

        if (!originalFileName.toLowerCase().endsWith(".json")) {
            throw new FileStorageException("Invalid file type — only .json files are allowed");
        }

        if (file.isEmpty()) {
            throw new FileStorageException("Uploaded file is empty");
        }

        Files.createDirectories(INPUT_ROOT);

        String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        Path filePath = INPUT_ROOT.resolve(storedFileName);

        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
            StreamUtils.copy(inputStream, outputStream);
        }

        JsonMetaDataEntity metaData = new JsonMetaDataEntity();
        metaData.setOriginalFileName(originalFileName);
        metaData.setStoredFileName(storedFileName);
        metaData.setMimeType(file.getContentType());
        metaData.setSize(file.getSize());
        metaData.setInputStorePath(filePath.toAbsolutePath().toString());
        metaData.setCreatedAt(LocalDateTime.now());
        metaData.setStatus(FileStatus.UPLOADED);
        metaData.setUploadedBy(uploadedBy);

        return jsonMetadataRepository.save(metaData);
    }

    public JsonMetaDataEntity jsonToCsv(Long id, String uploadedBy) throws IOException {

        JsonMetaDataEntity metaData = jsonMetadataRepository.findByIdAndUploadedBy(id, uploadedBy)
                .orElseThrow(() -> new FileNotFoundForUserException(
                        "File not found or you do not have access to it"));

        Path inputPath = Paths.get(metaData.getInputStorePath());

        if (!Files.exists(inputPath)) {
            metaData.setStatus(FileStatus.FAILED);
            jsonMetadataRepository.save(metaData);
            throw new FileStorageException("Source JSON file no longer exists on disk");
        }

        JsonNode rootNode;
        try (InputStream inputStream = Files.newInputStream(inputPath)) {
            rootNode = objectMapper.readTree(inputStream);
        } catch (IOException e) {
            metaData.setStatus(FileStatus.FAILED);
            jsonMetadataRepository.save(metaData);
            throw new FileStorageException("Stored file is not valid JSON: " + e.getMessage());
        }

        // Handle both a single JSON object and an array of objects
        List<Map<String, String>> rows = new ArrayList<>();
        if (rootNode.isArray()) {
            for (JsonNode element : rootNode) {
                rows.add(JsonFlattenerUtil.flatten(element));
            }
        } else {
            rows.add(JsonFlattenerUtil.flatten(rootNode));
        }

        if (rows.isEmpty()) {
            throw new FileStorageException("JSON file contains no data to convert");
        }

        Files.createDirectories(OUTPUT_ROOT);

        // Same UUID + base name as input, just .csv extension
        String csvFileName = metaData.getStoredFileName().replaceAll("\\.json$", "") + ".csv";
        Path outputPath = OUTPUT_ROOT.resolve(csvFileName);

        try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            CsvWriterUtil.writeCsv(writer, rows);
        }

        metaData.setOutputStorePath(outputPath.toAbsolutePath().toString());
        metaData.setStatus(FileStatus.PROCESSED);
        metaData.setProcessedAt(LocalDateTime.now());

        return jsonMetadataRepository.save(metaData);
    }

    public Path getDownloadablePath(Long id, String uploadedBy) {

        JsonMetaDataEntity metaData = jsonMetadataRepository.findByIdAndUploadedBy(id, uploadedBy)
                .orElseThrow(() -> new FileNotFoundForUserException(
                        "File not found or you do not have access to it"));

        if (metaData.getStatus() != FileStatus.PROCESSED || metaData.getOutputStorePath() == null) {
            throw new FileStorageException("File has not been converted to CSV yet — run conversion first");
        }

        Path outputPath = Paths.get(metaData.getOutputStorePath());

        if (!Files.exists(outputPath)) {
            throw new FileStorageException("CSV file no longer exists on disk");
        }

        return outputPath;
    }

    public String getCleanDownloadFileName(Long id, String uploadedBy) {
        JsonMetaDataEntity metaData = jsonMetadataRepository.findByIdAndUploadedBy(id, uploadedBy)
                .orElseThrow(() -> new FileNotFoundForUserException(
                        "File not found or you do not have access to it"));

        // originalFileName is "data.json" → we want "data.csv" for the download
        return metaData.getOriginalFileName().replaceAll("\\.json$", "") + ".csv";
    }
}