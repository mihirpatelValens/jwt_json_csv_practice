package com.example.demo.controller;

import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.UserJsonDTO;
import com.example.demo.service.FileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/file")
public class JsonFileController {

  private final ObjectMapper objectMapper;

  private final FileService fileService;

  JsonFileController(ObjectMapper objectMapper, FileService fileService) {
    this.objectMapper = objectMapper;
    this.fileService = fileService;
  }

  @PostMapping("json/upload")
  public ResponseEntity<?> uploadJson(@RequestParam("file") MultipartFile file) throws IOException {
    System.out.println("Controller uploadJson");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(401).body(
              new LoginResponseDTO(null, null, "Not authenticated")
      );
    }

//        UserJsonDTO jsonFile = objectMapper.readValue(file.getInputStream(), UserJsonDTO.class);
//
//        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//        Validator validator = factory.getValidator();
//
//        Set<ConstraintViolation<UserJsonDTO>> violations = validator.validate(jsonFile);
//
//        if(!violations.isEmpty()) {
//            List<String> errors = violations.stream().map(v->v.getPropertyPath() + ": "+ v.getMessage()).toList();
//            return ResponseEntity.badRequest().body(errors);
//        }

    try {
      JsonNode jsonTree = objectMapper.readTree(file.getInputStream());
      return ResponseEntity.ok(fileService.uploadJson(file, authentication.getName()));
    } catch (IOException e) {
      return ResponseEntity.badRequest().body("Invalid JSON file: " + e.getMessage());
    }

//        return ResponseEntity.ok(fileService.uploadJson(file,authentication.getName()));
//        return ResponseEntity.ok("Json File is Valid");
  }

  @GetMapping("json-to-csv/{id}")
  public void convertToCsv(@PathVariable Long id) {
    try {
      fileService.jsonToCsv(id);
    } catch (Exception e) {
      System.out.println("Error while converting to CSV: " + e.getMessage());
    }
  }
}
