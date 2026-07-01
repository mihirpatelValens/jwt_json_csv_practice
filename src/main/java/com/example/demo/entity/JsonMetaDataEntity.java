package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "json_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JsonMetaDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;   // e.g. "data.json" — what the user uploaded
    private String storedFileName;     // e.g. "a1b2c3_data.json" — actual name on disk
    private String mimeType;
    private Long size;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    private String inputStorePath;     // full path in /storage/input/
    private String outputStorePath;    // full path in /storage/output/, null until processed

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    private String uploadedBy;         // username from JWT, for ownership checks later
}