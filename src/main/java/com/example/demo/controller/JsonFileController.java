package com.example.demo.controller;

import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.UserJsonDTO;
import com.example.demo.service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/json")
public class JsonFileController {

    private final ObjectMapper objectMapper;

    private final FileService fileService;

    JsonFileController(ObjectMapper objectMapper, FileService fileService) {
        this.objectMapper = objectMapper;
        this.fileService = fileService;
    }

    @PostMapping("upload")
    public ResponseEntity<?> uploadJson(@RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("Controller uploadJson");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(
                    new LoginResponseDTO(null, null, "Not authenticated")
            );
        }

        UserJsonDTO jsonFile = objectMapper.readValue(file.getInputStream(), UserJsonDTO.class);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<UserJsonDTO>> violations = validator.validate(jsonFile);

        if(!violations.isEmpty()) {
            List<String> errors = violations.stream().map(v->v.getPropertyPath() + ": "+ v.getMessage()).toList();
            return ResponseEntity.badRequest().body(errors);
        }

        return ResponseEntity.ok(fileService.uploadJson(file,authentication.getName()));
//        return ResponseEntity.ok("Json File is Valid");
    }
}
