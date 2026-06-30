package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class JsonMetaData {
    private String fileName;
    private String mimeType;
    private Long size;
    private LocalDateTime createdAt;
    private String storePath;
}
