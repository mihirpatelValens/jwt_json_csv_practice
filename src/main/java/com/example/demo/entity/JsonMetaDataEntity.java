package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="json_metadata")
@Getter
@Setter
@NoArgsConstructor
public class JsonMetaDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String mimeType;
    private Long size;
    private LocalDateTime createdAt;
    private String storePath;
}
