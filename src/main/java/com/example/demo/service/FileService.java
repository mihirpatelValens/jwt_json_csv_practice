package com.example.demo.service;

import com.example.demo.dto.JsonMetaData;
import com.example.demo.entity.JsonMetaDataEntity;
import com.example.demo.repository.JsonMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileService {

  private final Path rootPath;

  private final JsonMetadataRepository jsonMetadataRepository;

  public FileService(JsonMetadataRepository jsonMetadataRepository) {
    this.jsonMetadataRepository = jsonMetadataRepository;
    this.rootPath = Paths.get("./files");
  }

  public JsonMetaDataEntity uploadJson(MultipartFile file, String name) throws IOException {
    System.out.println(name);

    String storedFilePath;
    try (InputStream inputStream = file.getInputStream()) {
      storedFilePath = storeFile(inputStream, file.getOriginalFilename());
    }

    JsonMetaDataEntity fileMetaData = new JsonMetaDataEntity();
    fileMetaData.setFileName(file.getOriginalFilename());
    fileMetaData.setMimeType(file.getContentType());
    fileMetaData.setSize(file.getSize());
    fileMetaData.setStorePath(storedFilePath);
    fileMetaData.setCreatedAt(LocalDateTime.now());
    JsonMetaDataEntity res = jsonMetadataRepository.save(fileMetaData);
    return res;
  }

  public void jsonToCsv(Long id) throws IOException {
    JsonMetaDataEntity metaDataEntity = jsonMetadataRepository.findById(id).orElse(null);

    if (metaDataEntity == null) {
      throw new IllegalArgumentException("Id not found");
    }

    System.out.println(metaDataEntity.getFileName() + " " + metaDataEntity.getStorePath());

  }

  private String storeFile(InputStream inputStream, String fileName) throws IOException {
    LocalDate today = LocalDate.now();

    Path fileDirectory = rootPath.resolve(
            today.getYear() + File.separator + String.format("%02d", today.getMonthValue())
    );
    Files.createDirectories(fileDirectory);

    if (!fileName.endsWith(".json")) {
      throw new IllegalArgumentException("Invalid file type, only .json files are allowed.");
    }

    String storedFileName = UUID.randomUUID().toString() + "_" + fileName;
    Path filePath = fileDirectory.resolve(storedFileName);

    try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
      StreamUtils.copy(inputStream, outputStream);
    }

    return filePath.toAbsolutePath().toString(); // return relative path
  }
}
